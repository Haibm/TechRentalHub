package com.techrentalhub.admin.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @GetMapping("/revenue-trend")
    public ResponseEntity<List<DailyStatDto>> getRevenueTrend(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(dashboardService.getDailyRevenueTrend(days));
    }

    @GetMapping("/monthly-revenue")
    public ResponseEntity<List<MonthlyStatDto>> getMonthlyRevenue(@RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(dashboardService.getMonthlyRevenueTrend(months));
    }

    @GetMapping("/order-breakdown")
    public ResponseEntity<Map<String, Long>> getOrderBreakdown() {
        return ResponseEntity.ok(dashboardService.getOrderStatusBreakdown());
    }
}
