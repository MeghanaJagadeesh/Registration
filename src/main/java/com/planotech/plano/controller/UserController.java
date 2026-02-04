package com.planotech.plano.controller;

import com.planotech.plano.auth.UserPrincipal;
import com.planotech.plano.exception.CustomJwtException;
import com.planotech.plano.exception.UserNotExistsException;
import com.planotech.plano.model.User;
import com.planotech.plano.response.UserProfileResponse;
import com.planotech.plano.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password) {
        return userService.login(email, password);
    }

    @PostMapping("/forgot/password/request")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        return userService.forgotPassword(email);
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String otp, @RequestParam String newPassword, @RequestParam String email) {
        return userService.resetPassword(otp, newPassword, email);
    }

    @GetMapping("/auth/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal userDetails) {
       return userService.getProfile(userDetails.getUser());
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        return userService.refreshToken(request);
    }


}
