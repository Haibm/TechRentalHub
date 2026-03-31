package com.techrentalhub.admin.kpi;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffKpiResponse {
    private Long adminId;
    private String adminName;
    private String adminEmail;
    private long ordersProcessed;     // Số đơn đã xử lý (mark-renting/complete/cancel)
    private long rentingApproved;     // Số đơn đã bàn giao (RENTING)
    private long rentingCompleted;    // Số đơn đã hoàn tất (COMPLETED)
    private long ordersCanceled;      // Số đơn đã hủy
    private Double avgProcessingHours; // TB giờ từ lúc đơn tạo đến khi admin xử lý
    private String performanceGrade;  // Xếp loại: A/B/C
}
