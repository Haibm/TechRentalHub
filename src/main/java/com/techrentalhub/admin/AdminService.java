package com.techrentalhub.admin;

import com.techrentalhub.admin.dto.StatsResponse;
import com.techrentalhub.admin.dto.TopDeviceDto;
import com.techrentalhub.order.OrderRepository;
import com.techrentalhub.order.OrderStatus;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public StatsResponse getStats() {
        long total = orderRepository.count();
        long pending = orderRepository.countByStatus(OrderStatus.PENDING_DEPOSIT);
        long depositPaid = orderRepository.countByStatus(OrderStatus.DEPOSIT_PAID);
        long completed = orderRepository.countByStatus(OrderStatus.COMPLETED);
        Double revenue = orderRepository.sumCollectedDeposits();
        long activeDevices = orderRepository.findActiveDeviceIds().size();
        long totalUsers = userRepository.count();

        return StatsResponse.builder()
                .totalOrders(total)
                .pendingOrders(pending)
                .depositPaidOrders(depositPaid)
                .completedOrders(completed)
                .totalRevenue(revenue != null ? revenue : 0.0)
                .activeDevices(activeDevices)
                .totalUsers(totalUsers)
                .build();
    }

    public List<TopDeviceDto> getTopDevices() {
        List<Object[]> raw = orderRepository.findTop5DevicesByOrders();
        List<TopDeviceDto> result = new ArrayList<>();
        for (Object[] row : raw) {
            TopDeviceDto dto = new TopDeviceDto();
            dto.setDeviceId(((Number) row[0]).longValue());
            dto.setDeviceName((String) row[1]);
            dto.setCategory(row[2] != null ? (String) row[2] : "N/A");
            dto.setOrderCount(((Number) row[3]).longValue());
            dto.setTotalRevenue(row[4] != null ? ((Number) row[4]).doubleValue() : 0.0);
            result.add(dto);
        }
        return result;
    }

    // ============================================================
    // Quản lý vòng đời đơn thuê
    // ============================================================

    @org.springframework.transaction.annotation.Transactional
    public String markAsRenting(Long orderId, Long adminId) {
        com.techrentalhub.order.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn #" + orderId));

        if (order.getStatus() != com.techrentalhub.order.OrderStatus.DEPOSIT_PAID) {
            throw new RuntimeException("Đơn hàng phải ở trạng thái DEPOSIT_PAID mới có thể bàn giao máy. Hiện tại: " + order.getStatus());
        }

        order.setStatus(com.techrentalhub.order.OrderStatus.RENTING);
        order.setProcessedByAdminId(adminId);
        order.setProcessedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return "✓ Đã cập nhật đơn #" + orderId + " sang trạng thái RENTING (Đã giao máy cho khách).";
    }

    @org.springframework.transaction.annotation.Transactional
    public String completeReturn(Long orderId, java.time.LocalDate actualReturnDate, Long adminId) {
        com.techrentalhub.order.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn #" + orderId));

        if (order.getStatus() != com.techrentalhub.order.OrderStatus.RENTING) {
            throw new RuntimeException("Đơn hàng phải ở trạng thái RENTING mới có thể xác nhận trả máy.");
        }

        order.setActualReturnDate(actualReturnDate);
        order.setStatus(com.techrentalhub.order.OrderStatus.COMPLETED);

        // Tracking admin (nếu đơn chưa được track bởi người mark-renting)
        if (order.getProcessedByAdminId() == null) {
            order.setProcessedByAdminId(adminId);
            order.setProcessedAt(java.time.LocalDateTime.now());
        }

        // Tính phụ đường phí: nếu khách trả trễ
        if (actualReturnDate.isAfter(order.getEndDate())) {
            long lateDays = java.time.temporal.ChronoUnit.DAYS.between(order.getEndDate(), actualReturnDate);
            double dailyRate = order.getTotalAmount() / order.getRentalDays();
            double lateFee = lateDays * dailyRate * 1.5; // 150% giá cước ngày
            order.setLateFee(lateFee);
            orderRepository.save(order);
            return String.format("✓ Hoàn tất đơn #%d. Trả trễ %d ngày. Phụ đường phí: %,.0f VNĐ", orderId, lateDays, lateFee);
        }

        orderRepository.save(order);
        return "✓ Hoàn tất đơn #" + orderId + ". Khách trả máy đúng hạn, không có phụ phí.";
    }

    @org.springframework.transaction.annotation.Transactional
    public String cancelOrder(Long orderId, Long adminId) {
        com.techrentalhub.order.Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn #" + orderId));

        if (order.getStatus() == com.techrentalhub.order.OrderStatus.COMPLETED ||
            order.getStatus() == com.techrentalhub.order.OrderStatus.RENTING) {
            throw new RuntimeException("Không thể hủy đơn đang ở trạng thái: " + order.getStatus());
        }

        order.setStatus(com.techrentalhub.order.OrderStatus.CANCELED);
        order.setProcessedByAdminId(adminId);
        order.setProcessedAt(java.time.LocalDateTime.now());
        orderRepository.save(order);
        return "✓ Đơn #" + orderId + " đã bị hủy thành công.";
    }
}
