package com.techrentalhub.admin.kpi;

import com.techrentalhub.order.OrderRepository;
import com.techrentalhub.order.OrderStatus;
import com.techrentalhub.user.User;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffKpiService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public List<StaffKpiResponse> getAllStaffKpi() {
        // Giả sử các user có role ADMIN là staff
        // Trong thực tế nên filter theo Role
        List<User> admins = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && u.getRole().name().equals("ADMIN"))
                .collect(Collectors.toList());

        return admins.stream()
                .map(this::calculateKpiForAdmin)
                .collect(Collectors.toList());
    }

    public StaffKpiResponse getKpiByAdminEmail(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy admin: " + email));
        return calculateKpiForAdmin(admin);
    }

    private StaffKpiResponse calculateKpiForAdmin(User admin) {
        Long adminId = admin.getId();
        long totalProcessed = orderRepository.countByProcessedByAdminId(adminId);
        long rentingCount = orderRepository.countByProcessedByAdminIdAndStatus(adminId, OrderStatus.RENTING);
        long completedCount = orderRepository.countByProcessedByAdminIdAndStatus(adminId, OrderStatus.COMPLETED);
        long canceledCount = orderRepository.countByProcessedByAdminIdAndStatus(adminId, OrderStatus.CANCELED);
        
        Double avgHours = orderRepository.getAvgProcessingTimeHours(adminId);
        if (avgHours == null) avgHours = 0.0;

        return StaffKpiResponse.builder()
                .adminId(adminId)
                .adminName(admin.getFullName())
                .adminEmail(admin.getEmail())
                .ordersProcessed(totalProcessed)
                .rentingApproved(rentingCount)
                .rentingCompleted(completedCount)
                .ordersCanceled(canceledCount)
                .avgProcessingHours(Math.round(avgHours * 10.0) / 10.0)
                .performanceGrade(calculateGrade(totalProcessed, avgHours))
                .build();
    }

    private String calculateGrade(long count, double hours) {
        if (count == 0) return "N/A";
        if (count > 50 && hours < 2) return "A+ (Excellent)";
        if (count > 20 && hours < 12) return "A (Good)";
        if (count > 5 && hours < 24) return "B (Normal)";
        return "C (Needs Improvement)";
    }
}
