package com.techrentalhub.auth;

import com.techrentalhub.auth.dto.AuthenticationRequest;
import com.techrentalhub.auth.dto.AuthenticationResponse;
import com.techrentalhub.auth.dto.RegisterRequest;
import com.techrentalhub.user.Role;
import com.techrentalhub.user.User;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthenticationResponse register(RegisterRequest request) {
        if (repository.existsByUsername(request.getUsername())) {
            return AuthenticationResponse.builder().message("Tên đăng nhập đã tồn tại").build();
        }
        if (repository.existsByEmail(request.getEmail())) {
            return AuthenticationResponse.builder().message("Email đã tồn tại").build();
        }

        var user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.ROLE_USER)
                .enabled(false) // Trạng thái bị khóa ban đầu
                .build();
        
        repository.save(user);

        // Phát sinh mã vào Memory Cache và Gửi Mail
        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);

        return AuthenticationResponse.builder()
                .message("Đăng ký tạm thời hoàn tất! Chúng tôi đã gửi 1 Email chứa mã OTP (6 số) vào hòm thư " + user.getEmail() + ". Vui lòng xác thực mã này để mở khóa tài khoản.")
                .build();
    }

    public AuthenticationResponse verifyOtp(String email, String otp) {
        if (otpService.verifyOtp(email, otp)) {
            User user = repository.findByEmail(email).orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
            user.setEnabled(true);
            repository.save(user);

            var jwtToken = jwtService.generateToken(user);
            return AuthenticationResponse.builder()
                    .accessToken(jwtToken)
                    .message("Kích hoạt tài khoản thành công! Tính năng Anti-Spam tự động xóa OTP khỏi Cache.")
                    .build();
        }
        return AuthenticationResponse.builder().message("Xác thực thất bại.").build();
    }

    public AuthenticationResponse forgotPassword(String email) {
        User user = repository.findByEmail(email).orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);
        return AuthenticationResponse.builder()
                .message("Hệ thống đã gửi mã xác nhận đổi mật khẩu tới email của bạn.")
                .build();
    }

    public AuthenticationResponse resetPassword(String email, String otp, String newPassword) {
        if (otpService.verifyOtp(email, otp)) {
            User user = repository.findByEmail(email).orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại."));
            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);
            return AuthenticationResponse.builder()
                    .message("Lấy lại mật khẩu thành công! Xin hãy đăng nhập lại hệ thống bằng Pass mới.")
                    .build();
        }
        return AuthenticationResponse.builder().message("Xác thực mã OTP thất bại.").build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = repository.findByUsername(request.getUsername())
                .orElseThrow();
                
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .message("Đăng nhập thành công")
                .build();
    }
}
