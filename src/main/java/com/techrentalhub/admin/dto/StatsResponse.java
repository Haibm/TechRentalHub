package com.techrentalhub.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private long totalOrders;
    private long pendingOrders;
    private long depositPaidOrders;
    private long completedOrders;
    private double totalRevenue;       // Tổng tiền cọc đã thu
    private long activeDevices;        // Thiết bị đang có đơn đang thuê
    private long totalUsers;
}
