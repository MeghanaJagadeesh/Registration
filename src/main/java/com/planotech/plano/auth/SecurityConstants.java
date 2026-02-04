package com.planotech.plano.auth;

public class SecurityConstants {
    private SecurityConstants() {}

    public static final String[] PUBLIC_URLS = {
            "/user/login",
            "/user/register",
            "/user/refresh",
            "/user/forgot/password/request",
            "/user/auth/reset-password",
            "/public/events/**",
            "/file/upload",
            "/test"
    };
}

