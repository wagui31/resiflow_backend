package com.resiflow.controller;

import com.resiflow.dto.ForgotPasswordRequestCodeRequest;
import com.resiflow.dto.ForgotPasswordRequestCodeResponse;
import com.resiflow.dto.ForgotPasswordResetPasswordRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeRequest;
import com.resiflow.dto.ForgotPasswordVerifyCodeResponse;
import com.resiflow.dto.LoginRequest;
import com.resiflow.dto.LoginResponse;
import com.resiflow.dto.LogoutRequest;
import com.resiflow.dto.RegisterRequest;
import com.resiflow.dto.UserResponse;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.AuthService;
import com.resiflow.service.ForgotPasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/auth", "/api/auth"})
public class AuthController {

    private final AuthService authService;
    private final ForgotPasswordService forgotPasswordService;

    public AuthController(final AuthService authService, final ForgotPasswordService forgotPasswordService) {
        this.authService = authService;
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody final LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/forgot-password/request-code")
    public ResponseEntity<ForgotPasswordRequestCodeResponse> requestForgotPasswordCode(
            @RequestBody final ForgotPasswordRequestCodeRequest request
    ) {
        return ResponseEntity.ok(forgotPasswordService.requestCode(request));
    }

    @PostMapping("/forgot-password/verify-code")
    public ResponseEntity<ForgotPasswordVerifyCodeResponse> verifyForgotPasswordCode(
            @RequestBody final ForgotPasswordVerifyCodeRequest request
    ) {
        return ResponseEntity.ok(forgotPasswordService.verifyCode(request));
    }

    @PostMapping("/forgot-password/reset-password")
    public ResponseEntity<Void> resetForgottenPassword(
            @RequestBody final ForgotPasswordResetPasswordRequest request
    ) {
        forgotPasswordService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody final RegisterRequest request,
            final HttpServletRequest httpRequest
    ) {
        String clientPlatform = httpRequest.getHeader("X-Client-Platform");
        return ResponseEntity.ok(UserResponse.fromUser(authService.register(request, clientPlatform)));
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> logout(
            @RequestBody final LogoutRequest request,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        authService.logout(authenticatedUser, request);
        return ResponseEntity.noContent().build();
    }
}
