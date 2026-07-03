package com.project.codinviec_payment_service.constant;

public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String[] API_PUBLIC_URLS = {
            "/api/payment/vnpay/callback",
            "/api/payment/vnpay/ipn",
    };

    public static final String[] API_PUBLIC_GET_URLS = {
            "/api/service-product/**",
            "/api/payment-method/**",
    };

    public static final String[] USER_URLS = {
            "/api/payment/**",
            "/api/payment-method/**",
            "/api/service-product/**",
    };
}
