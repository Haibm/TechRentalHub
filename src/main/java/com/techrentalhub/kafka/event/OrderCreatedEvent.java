package com.techrentalhub.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private Long orderId;
    private String userEmail;
    private String userFullName;
    private String deviceName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double depositAmount;
    private String vnpayTxnRef;
}
