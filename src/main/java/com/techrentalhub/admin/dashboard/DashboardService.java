package com.techrentalhub.admin.dashboard;

import com.techrentalhub.device.DeviceRepository;
import com.techrentalhub.order.OrderRepository;
import com.techrentalhub.order.OrderStatus;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;

    public List<DailyStatDto> getDailyRevenueTrend(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Object[]> results = orderRepository.getDailyStats(since);
        
        return results.stream().map(row -> DailyStatDto.builder()
                .date(row[0].toString())
                .orderCount(((Number) row[1]).longValue())
                .revenue(((Number) row[2]).doubleValue())
                .build()
        ).collect(Collectors.toList());
    }

    public List<MonthlyStatDto> getMonthlyRevenueTrend(int months) {
        LocalDateTime since = LocalDateTime.now().minusMonths(months);
        List<Object[]> results = orderRepository.getMonthlyStats(since);

        return results.stream().map(row -> MonthlyStatDto.builder()
                .month(row[0].toString())
                .orderCount(((Number) row[1]).longValue())
                .revenue(((Number) row[2]).doubleValue())
                .completedCount(((Number) row[3]).longValue())
                .build()
        ).collect(Collectors.toList());
    }

    public Map<String, Long> getOrderStatusBreakdown() {
        return Map.of(
            "PENDING_DEPOSIT", orderRepository.countByStatus(OrderStatus.PENDING_DEPOSIT),
            "DEPOSIT_PAID", orderRepository.countByStatus(OrderStatus.DEPOSIT_PAID),
            "RENTING", orderRepository.countByStatus(OrderStatus.RENTING),
            "COMPLETED", orderRepository.countByStatus(OrderStatus.COMPLETED),
            "CANCELED", orderRepository.countByStatus(OrderStatus.CANCELED)
        );
    }

    public Map<String, Object> getSummary() {
        long totalOrders = orderRepository.count();
        long totalDevices = deviceRepository.count();
        long totalUsers = userRepository.count();
        
        // Tính tỷ lệ sử dụng máy: (máy trong đơn DEPOSIT_PAID hoặc RENTING) / tổng máy
        long activeDevices = orderRepository.countByStatus(OrderStatus.DEPOSIT_PAID) 
                           + orderRepository.countByStatus(OrderStatus.RENTING);
        
        double utilizationRate = totalDevices > 0 ? (double) activeDevices / totalDevices * 100 : 0;

        return Map.of(
            "totalOrders", totalOrders,
            "totalDevices", totalDevices,
            "totalUsers", totalUsers,
            "activeDevices", activeDevices,
            "utilizationRate", Math.round(utilizationRate * 10.0) / 10.0,
            "statusBreakdown", getOrderStatusBreakdown()
        );
    }
}
