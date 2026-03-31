package com.techrentalhub.admin.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyStatDto {
    private String month;       // Format: yyyy-MM
    private long orderCount;    // Số đơn mới trong tháng
    private double revenue;     // Doanh thu (tiền cọc) trong tháng
    private long completedCount; // Số đơn hoàn thành trong tháng
}
