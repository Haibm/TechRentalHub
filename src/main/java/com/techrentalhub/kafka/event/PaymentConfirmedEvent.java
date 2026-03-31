package com.techrentalhub.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmedEvent {
    private Long orderId;
    private String userEmail;
    private String userFullName;
    private String deviceName;
    private String vnpayTxnRef;
    private Double depositAmount;
}
