package com.techrentalhub.admin;

import com.techrentalhub.admin.dto.StatsResponse;
import com.techrentalhub.admin.dto.TopDeviceDto;
import com.techrentalhub.ai.ChatRequest;

import com.techrentalhub.order.Order;
import com.techrentalhub.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final com.techrentalhub.ai.AdminCopilotService adminCopilotService;
    private final OrderRepository orderRepository;

    /**
     * GET /api/admin/stats
     * Trả về KPI tổng quan: tổng đơn, doanh thu, số user, thiết bị đang hoạt động.
     */
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }

    /**
     * GET /api/admin/top-devices
     * Top 5 thiết bị được đặt thuê nhiều nhất.
     */
    @GetMapping("/top-devices")
    public ResponseEntity<List<TopDeviceDto>> getTopDevices() {
        return ResponseEntity.ok(adminService.getTopDevices());
    }

    /**
     * GET /api/admin/orders?page=0&size=20
     * Xem tất cả đơn hàng trong hệ thống (có phân trang).
     */
    @GetMapping("/orders")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(orderRepository.findAll(PageRequest.of(page, size)));
    }

    /**
     * POST /api/admin/ai/chat
     * Chat với Admin Copilot (Spring AI). AI có bộ nhớ hội thoại và context DB.
     */
    @PostMapping("/ai/chat")
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody ChatRequest request,
            Authentication authentication) {
        String answer = adminCopilotService.ask(request.getQuestion(), authentication.getName());
        return ResponseEntity.ok(Map.of(
                "question", request.getQuestion(),
                "answer", answer
        ));
    }

    /**
     * DELETE /api/admin/ai/chat/history
     * Xóa lịch sử trò chuyện của admin hiện tại.
     */
    @DeleteMapping("/ai/chat/history")
    public ResponseEntity<Map<String, String>> clearChatHistory(Authentication authentication) {
        adminCopilotService.clearHistory(authentication.getName());
        return ResponseEntity.ok(Map.of("message", "✓ Đã xóa lịch sử trò chuyện của bạn."));
    }
}
