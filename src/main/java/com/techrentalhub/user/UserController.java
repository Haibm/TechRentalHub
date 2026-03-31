package com.techrentalhub.user;

import com.techrentalhub.order.Order;
import com.techrentalhub.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    /**
     * GET /api/profile
     * Lấy thông tin cá nhân của user đang đăng nhập.
     */
    @GetMapping("/api/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/profile
     * Cập nhật fullName và phone.
     */
    @PutMapping("/api/profile")
    public ResponseEntity<Map<String, String>> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user.setPhone(request.getPhone());
        }

        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "✓ Cập nhật thông tin tài khoản thành công!"));
    }

    /**
     * GET /api/orders/my-orders?page=0&size=10
     * Xem lịch sử đơn thuê của chính mình (chỉ thấy đơn của mình).
     */
    @GetMapping("/api/orders/my-orders")
    public ResponseEntity<Page<Order>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        Page<Order> orders = orderRepository.findByUserEmailOrderByCreatedAtDesc(
                email, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(orders);
    }
}
