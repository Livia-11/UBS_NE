package com.ubs.billing.controller;

import com.ubs.billing.dto.Dtos;
import com.ubs.billing.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Public signup/login/OTP endpoints plus authenticated password changes.")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Customer self-signup", description = "Public endpoint. Requires a unique 16-digit nationalId. Creates an unverified ROLE_CUSTOMER login and inactive billing customer profile, then emails an OTP. Verify OTP before login.")
    public Dtos.AuthResponse signup(@Valid @RequestBody Dtos.SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT", description = "If mustChangePassword is true, call /api/auth/change-password before using other secured APIs.")
    public Dtos.AuthResponse login(@Valid @RequestBody Dtos.LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify customer signup OTP", description = "Public endpoint. Customers must verify the OTP sent by email before they can log in.")
    public Dtos.MessageResponse verifyOtp(@Valid @RequestBody Dtos.VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change current or temporary password", description = "Authenticated endpoint. Required after first login for users created by an administrator.")
    public Dtos.MessageResponse changePassword(Authentication authentication, @Valid @RequestBody Dtos.ChangePasswordRequest request) {
        return authService.changePassword(authentication, request);
    }
}
