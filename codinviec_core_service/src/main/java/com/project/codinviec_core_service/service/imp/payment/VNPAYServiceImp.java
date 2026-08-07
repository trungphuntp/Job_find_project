package com.project.codinviec_core_service.service.imp.payment;
import com.project.codinviec_core_service.enums.ResourceErrorCode;
import com.project.codinviec_core_service.exception.AppException;

import com.project.codinviec_core_service.config.VNPAYConfig;
import com.project.codinviec_core_service.dto.vnpay.VNPAYCallBackResponseDTO;
import com.project.codinviec_core_service.dto.vnpay.VNPAYPaymentResponseDTO;
import com.project.codinviec_core_service.entity.auth.User;
import com.project.codinviec_core_service.entity.payment.Payment;
import com.project.codinviec_core_service.entity.payment.PaymentMethod;
import com.project.codinviec_core_service.entity.payment.PaymentStatus;
import com.project.codinviec_core_service.entity.payment.ServiceProduct;
import com.project.codinviec_core_service.repository.auth.UserRepository;
import com.project.codinviec_core_service.repository.payment.PaymentMethodRepository;
import com.project.codinviec_core_service.repository.payment.PaymentRepository;
import com.project.codinviec_core_service.repository.payment.PaymentStatusRepository;
import com.project.codinviec_core_service.repository.payment.ServiceProductRepository;
import com.project.codinviec_core_service.request.payment.PaymentRequest;
import com.project.codinviec_core_service.service.payment.VNPAYService;
import com.project.codinviec_core_service.util.security.VNPayHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class VNPAYServiceImp implements VNPAYService {
    private final VNPAYConfig vnpayConfig;
    private final VNPayHelper vnPayHelper;
    private final PaymentRepository paymentRepository;
    private final ServiceProductRepository serviceProductRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentStatusRepository paymentStatusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public VNPAYPaymentResponseDTO createPaymentUrl(PaymentRequest paymentRequest, HttpServletRequest httpRequest) {
        // Bước 1: Validate ServiceProduct
        ServiceProduct serviceProduct = serviceProductRepository.findById(paymentRequest.getServiceProductId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Service Product"));

        // Bước 2: Validate User
        User user = userRepository.findById(paymentRequest.getUserId())
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy User"));

        // Bước 3: Tìm PaymentMethod VNPAY (id = 9 theo database)
        PaymentMethod vnpayMethod = paymentMethodRepository.findByNameIgnoreCase("VNPAY")
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Payment Method VNPAY"));

        // Bước 4: Tìm PaymentStatus PENDING (id = 1 theo database)
        PaymentStatus pendingStatus = paymentStatusRepository.findByNameIgnoreCase("Pending")
                .orElseThrow(() -> new AppException(ResourceErrorCode.NOT_FOUND, "Không tìm thấy Payment Status PENDING"));

        // Bước 5: Tạo mã giao dịch ngẫu nhiên (8 chữ số)
        String randomNumber = vnPayHelper.getRandomNumber(8);

        // Bước 6: Tạo orderInfo (mô tả đơn hàng)
        String orderInfo = paymentRequest.getDescription() != null && !paymentRequest.getDescription().isEmpty()
                ? paymentRequest.getDescription()
                : "Thanh toán đơn hàng: " + serviceProduct.getName();

        // Bước 7: Tạo Payment record với status PENDING
        Payment payment = Payment.builder()
                .title("Thanh toán VNPAY - " + serviceProduct.getName())
                .description(orderInfo)
                .paymentMethod(vnpayMethod)
                .paymentStatus(pendingStatus)
                .serviceProduct(serviceProduct)
                .user(user)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        // Bước 8: Lưu Payment để lấy ID
        Payment savedPayment = paymentRepository.save(payment);

        // Bước 9: Tạo vnp_TxnRef với format: paymentId_randomNumber
        // Ví dụ: "123_45678901" - giúp dễ dàng tra cứu payment sau này
        String vnp_TxnRef = savedPayment.getId() + "_" + randomNumber;


        // Bước 10: Build URL thanh toán VNPAY
        String paymentUrl = buildPaymentUrl(
                vnp_TxnRef,
                serviceProduct.getPrice(),
                orderInfo,
                "other",
                httpRequest
        );

        // Bước 11: Trả về response
        return VNPAYPaymentResponseDTO.builder()
                .paymentUrl(paymentUrl)
                .vnpTxnRef(vnp_TxnRef)
                .build();
    }

    @Override
    public VNPAYCallBackResponseDTO handleIpn(HttpServletRequest request) {
        Map<String, String> params = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()[0]
                ));

        // 1. Verify checksum
        String vnpSecureHash = params.get("vnp_SecureHash");

        Map<String, String> vnpParams = new HashMap<>(params);
        vnpParams.remove("vnp_SecureHash");
        vnpParams.remove("vnp_SecureHashType");

        String checkSum = vnPayHelper.hashAllFields(vnpParams, vnpayConfig.getHashSecret());
        if (!checkSum.equals(vnpSecureHash)) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("97")
                    .message("Invalid signature")
                    .build();
        }

        // 2. Parse paymentId từ vnp_TxnRef
        String vnpTxnRef = params.get("vnp_TxnRef");
        if (vnpTxnRef == null || !vnpTxnRef.contains("_")) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("01")
                    .message("Invalid TxnRef")
                    .build();
        }

        Integer paymentId;
        try {
            paymentId = Integer.parseInt(vnpTxnRef.split("_")[0]);
        } catch (Exception e) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("01")
                    .message("Invalid paymentId")
                    .build();
        }

        // 3. Find payment (SAFE – không throw)
        Optional<Payment> optPayment = paymentRepository.findById(paymentId);
        if (optPayment.isEmpty()) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("01")
                    .message("Payment not found")
                    .build();
        }

        Payment payment = optPayment.get();

        // 4. Idempotent check (CỰC KỲ QUAN TRỌNG)
        if ("Completed".equalsIgnoreCase(payment.getPaymentStatus().getName())) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("00")
                    .message("Already processed")
                    .paymentId(paymentId)
                    .build();
        }

        // 5. Check amount
        long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
        long expectedAmount = (long) (payment.getServiceProduct().getPrice() * 100);

        if (vnpAmount != expectedAmount) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("04")
                    .message("Invalid amount")
                    .paymentId(paymentId)
                    .build();
        }

        // 6. Xử lý trạng thái
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            PaymentStatus success = paymentStatusRepository
                    .findByNameIgnoreCase("Completed")
                    .orElseThrow(); // chỉ throw ở đây vì lỗi data nội bộ

            payment.setPaymentStatus(success);
        } else {
            PaymentStatus failed = paymentStatusRepository
                    .findByNameIgnoreCase("Failed")
                    .orElseThrow();

            payment.setPaymentStatus(failed);
        }

        payment.setUpdatedDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // 7. Trả OK cho VNPay (BẮT BUỘC)
        return VNPAYCallBackResponseDTO.builder()
                .code("00")
                .message("Confirm Success")
                .paymentId(paymentId)
                .build();
    }


    public String buildPaymentUrl(String vnp_TxnRef, Double amount, String orderInfo,
                                  String orderType, HttpServletRequest request) {

        // Các tham số bắt buộc theo VNPAY API
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = vnpayConfig.getTmnCode();

        // VNPAY yêu cầu số tiền phải nhân 100 (ví dụ: 500000 VND -> 50000000)
        String vnp_Amount = String.valueOf((long) (amount * 100));
        String vnp_CurrCode = "VND";
        String vnp_IpAddr = vnPayHelper.getIpAddress(request);
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = vnpayConfig.getReturnUrl();
        String vnp_OrderType = orderType != null && !orderType.isEmpty() ? orderType : "other";

//        Tạm thời chưa có domain nên chưa thể làm IPN server call servel
//        String vnp_IpnUrl = vnpayConfig.getIpnUrl();

        // Tạo Map chứa tất cả tham số
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", vnp_CurrCode);
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", orderInfo);
        vnp_Params.put("vnp_OrderType", vnp_OrderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
//        vnp_Params.put("vnp_IpnUrl", vnp_IpnUrl);


        // Tạo thời gian tạo giao dịch (format: yyyyMMddHHmmss)
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        // Tạo thời gian hết hạn (15 phút sau)
        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Tạo chữ ký (hash) để bảo mật
        String vnp_SecureHash = vnPayHelper.hashAllFields(vnp_Params, vnpayConfig.getHashSecret());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

        // Build query string từ Map
        StringBuilder queryUrl = new StringBuilder();
        Iterator<Map.Entry<String, String>> itr = vnp_Params.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            queryUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII));
            queryUrl.append('=');
            queryUrl.append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII));
            if (itr.hasNext()) {
                queryUrl.append('&');
            }
        }

        // Trả về URL hoàn chỉnh
        return vnpayConfig.getPayUrl() + "?" + queryUrl;
    }

}
