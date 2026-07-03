package com.project.codinviec_payment_service.service.imp.payment;

import com.project.codinviec_payment_service.config.VNPAYConfig;
import com.project.codinviec_payment_service.dto.vnpay.VNPAYCallBackResponseDTO;
import com.project.codinviec_payment_service.dto.vnpay.VNPAYPaymentResponseDTO;
import com.project.codinviec_payment_service.entity.Payment;
import com.project.codinviec_payment_service.entity.PaymentMethod;
import com.project.codinviec_payment_service.entity.PaymentStatus;
import com.project.codinviec_payment_service.entity.ServiceProduct;
import com.project.codinviec_payment_service.exception.common.NotFoundIdExceptionHandler;
import com.project.codinviec_payment_service.repository.payment.PaymentMethodRepository;
import com.project.codinviec_payment_service.repository.payment.PaymentRepository;
import com.project.codinviec_payment_service.repository.payment.PaymentStatusRepository;
import com.project.codinviec_payment_service.repository.payment.ServiceProductRepository;
import com.project.codinviec_payment_service.request.payment.PaymentRequest;
import com.project.codinviec_payment_service.service.payment.VNPAYService;
import com.project.codinviec_payment_service.util.security.VNPayHelper;
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

    @Override
    @Transactional
    public VNPAYPaymentResponseDTO createPaymentUrl(PaymentRequest paymentRequest, HttpServletRequest httpRequest) {
        ServiceProduct serviceProduct = serviceProductRepository.findById(paymentRequest.getServiceProductId())
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Service Product"));

        PaymentMethod vnpayMethod = paymentMethodRepository.findByNameIgnoreCase("VNPAY")
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Payment Method VNPAY"));

        PaymentStatus pendingStatus = paymentStatusRepository.findByNameIgnoreCase("Pending")
                .orElseThrow(() -> new NotFoundIdExceptionHandler("Không tìm thấy Payment Status PENDING"));

        String randomNumber = vnPayHelper.getRandomNumber(8);

        String orderInfo = paymentRequest.getDescription() != null && !paymentRequest.getDescription().isEmpty()
                ? paymentRequest.getDescription()
                : "Thanh toán đơn hàng: " + serviceProduct.getName();

        Payment payment = Payment.builder()
                .title("Thanh toán VNPAY - " + serviceProduct.getName())
                .description(orderInfo)
                .paymentMethod(vnpayMethod)
                .paymentStatus(pendingStatus)
                .serviceProduct(serviceProduct)
                .userId(paymentRequest.getUserId())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        String vnp_TxnRef = savedPayment.getId() + "_" + randomNumber;

        String paymentUrl = buildPaymentUrl(
                vnp_TxnRef,
                serviceProduct.getPrice(),
                orderInfo,
                "other",
                httpRequest
        );

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

        Optional<Payment> optPayment = paymentRepository.findById(paymentId);
        if (optPayment.isEmpty()) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("01")
                    .message("Payment not found")
                    .build();
        }

        Payment payment = optPayment.get();

        if ("Completed".equalsIgnoreCase(payment.getPaymentStatus().getName())) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("00")
                    .message("Already processed")
                    .paymentId(paymentId)
                    .build();
        }

        long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
        long expectedAmount = (long) (payment.getServiceProduct().getPrice() * 100);

        if (vnpAmount != expectedAmount) {
            return VNPAYCallBackResponseDTO.builder()
                    .code("04")
                    .message("Invalid amount")
                    .paymentId(paymentId)
                    .build();
        }

        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
            PaymentStatus success = paymentStatusRepository
                    .findByNameIgnoreCase("Completed")
                    .orElseThrow();
            payment.setPaymentStatus(success);
        } else {
            PaymentStatus failed = paymentStatusRepository
                    .findByNameIgnoreCase("Failed")
                    .orElseThrow();
            payment.setPaymentStatus(failed);
        }

        payment.setUpdatedDate(LocalDateTime.now());
        paymentRepository.save(payment);

        return VNPAYCallBackResponseDTO.builder()
                .code("00")
                .message("Confirm Success")
                .paymentId(paymentId)
                .build();
    }

    public String buildPaymentUrl(String vnp_TxnRef, Double amount, String orderInfo,
                                  String orderType, HttpServletRequest request) {
        String vnp_Version = "2.1.0";
        String vnp_Command = "pay";
        String vnp_TmnCode = vnpayConfig.getTmnCode();
        String vnp_Amount = String.valueOf((long) (amount * 100));
        String vnp_CurrCode = "VND";
        String vnp_IpAddr = vnPayHelper.getIpAddress(request);
        String vnp_Locale = "vn";
        String vnp_ReturnUrl = vnpayConfig.getReturnUrl();
        String vnp_OrderType = orderType != null && !orderType.isEmpty() ? orderType : "other";

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

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        String vnp_SecureHash = vnPayHelper.hashAllFields(vnp_Params, vnpayConfig.getHashSecret());
        vnp_Params.put("vnp_SecureHash", vnp_SecureHash);

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

        return vnpayConfig.getPayUrl() + "?" + queryUrl;
    }
}
