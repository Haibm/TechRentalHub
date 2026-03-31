package com.techrentalhub.admin.dashboard;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DailyStatDto {
    private String date;        // Format: yyyy-MM-dd
    private long orderCount;    // Số đơn mới trong ngày
    private double revenue;     // Doanh thu (tiền cọc) trong ngày
}
