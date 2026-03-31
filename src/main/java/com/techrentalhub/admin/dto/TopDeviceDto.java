package com.techrentalhub.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopDeviceDto {
    private Long deviceId;
    private String deviceName;
    private String category;
    private Long orderCount;
    private Double totalRevenue;
}
