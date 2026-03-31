package com.techrentalhub.auth;

import com.techrentalhub.auth.dto.AuthenticationRequest;
import com.techrentalhub.auth.dto.AuthenticationResponse;
import com.techrentalhub.auth.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @RequestBody com.techrentalhub.auth.dto.OtpRequest request
    ) {
        return ResponseEntity.ok(service.verifyOtp(request.getEmail(), request.getOtp()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthenticationResponse> forgotPassword(
            @RequestBody com.techrentalhub.auth.dto.OtpRequest request
    ) {
        return ResponseEntity.ok(service.forgotPassword(request.getEmail()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthenticationResponse> resetPassword(
            @RequestBody com.techrentalhub.auth.dto.ResetPasswordRequest request
    ) {
        return ResponseEntity.ok(service.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword()));
    }
}
