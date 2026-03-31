package com.techrentalhub.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderNotificationDto {
    private Long orderId;
    private String customerName;
    private String deviceName;
    private Double depositAmount;
    private String message;
    private String timestamp;
}
