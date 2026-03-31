package com.techrentalhub.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/rental")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class RentalManagementController {

    private final AdminService adminService;
    private final com.techrentalhub.user.UserRepository userRepository;

    private Long getAdminId(org.springframework.security.core.Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .map(com.techrentalhub.user.User::getId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    /**
     * POST /api/admin/rental/{id}/mark-renting
     * Admin xác nhận đã bàn giao máy cho khách → DEPOSIT_PAID → RENTING
     */
    @PostMapping("/{id}/mark-renting")
    public ResponseEntity<Map<String, String>> markRenting(
            @PathVariable Long id, 
            org.springframework.security.core.Authentication auth) {
        try {
            Long adminId = getAdminId(auth);
            String msg = adminService.markAsRenting(id, adminId);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/rental/{id}/complete-return?actualReturnDate=2025-04-15
     * Admin xác nhận khách đã trả máy → tính late fee (nếu có) → COMPLETED
     */
    @PostMapping("/{id}/complete-return")
    public ResponseEntity<Map<String, String>> completeReturn(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate actualReturnDate,
            org.springframework.security.core.Authentication auth) {
        try {
            Long adminId = getAdminId(auth);
            String msg = adminService.completeReturn(id, actualReturnDate, adminId);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * POST /api/admin/rental/{id}/cancel
     * Admin hủy đơn hàng (chỉ khi chưa RENTING hoặc COMPLETED)
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @PathVariable Long id, 
            org.springframework.security.core.Authentication auth) {
        try {
            Long adminId = getAdminId(auth);
            String msg = adminService.cancelOrder(id, adminId);
            return ResponseEntity.ok(Map.of("message", msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
