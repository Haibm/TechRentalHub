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
                .build();
        
        repository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .message("Đăng ký thành công")
                .build();
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
