package com.techrentalhub.kafka.consumer;

import com.techrentalhub.auth.EmailService;
import com.techrentalhub.kafka.KafkaTopicConfig;
import com.techrentalhub.kafka.event.OrderCreatedEvent;
import com.techrentalhub.kafka.event.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = KafkaTopicConfig.ORDER_EVENTS_TOPIC, groupId = "techrentalhub-group")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("[KAFKA CONSUMER] Nhận được OrderCreatedEvent → Đơn #{} của {}", 
                event.getOrderId(), event.getUserEmail());

        String subject = "[TechRentalHub] Đặt thuê thiết bị thành công - Vui lòng thanh toán cọc";
        String body = buildOrderCreatedEmail(event);
        emailService.sendEmail(event.getUserEmail(), subject, body);

        log.info("[KAFKA CONSUMER] ✓ Đã gửi email xác nhận đơn #{} tới {}", 
                event.getOrderId(), event.getUserEmail());
    }

    @KafkaListener(topics = KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, groupId = "techrentalhub-group")
    public void handlePaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("[KAFKA CONSUMER] Nhận được PaymentConfirmedEvent → TxnRef: {}", 
                event.getVnpayTxnRef());

        String subject = "[TechRentalHub] Xác nhận thanh toán cọc thành công!";
        String body = buildPaymentConfirmedEmail(event);
        emailService.sendEmail(event.getUserEmail(), subject, body);

        log.info("[KAFKA CONSUMER] ✓ Đã gửi email xác nhận thanh toán TxnRef: {} tới {}", 
                event.getVnpayTxnRef(), event.getUserEmail());
    }

    private String buildOrderCreatedEmail(OrderCreatedEvent event) {
        return String.format("""
                Xin chào %s,
                
                Đơn đặt thuê thiết bị của bạn đã được ghi nhận thành công!
                
                ▸ Thiết bị: %s
                ▸ Ngày nhận: %s
                ▸ Ngày trả: %s
                ▸ Tiền cọc cần thanh toán: %,.0f VNĐ
                ▸ Mã giao dịch: %s
                
                Hệ thống đã tạo link thanh toán VNPAY. Vui lòng hoàn tất thanh toán cọc trong vòng 15 phút 
                để giữ lịch thuê máy.
                
                Trân trọng,
                TechRentalHub Team
                """,
                event.getUserFullName(),
                event.getDeviceName(),
                event.getStartDate(),
                event.getEndDate(),
                event.getDepositAmount(),
                event.getVnpayTxnRef()
        );
    }

    private String buildPaymentConfirmedEmail(PaymentConfirmedEvent event) {
        return String.format("""
                Xin chào %s,
                
                Thanh toán cọc của bạn đã được xác nhận thành công!
                
                ▸ Thiết bị: %s
                ▸ Số tiền cọc: %,.0f VNĐ
                ▸ Mã giao dịch VNPAY: %s
                
                Bạn có thể đến nhận thiết bị theo lịch hẹn. Hợp đồng điện tử sẵn sàng nếu bạn cần in.
                
                Trân trọng,
                TechRentalHub Team
                """,
                event.getUserFullName(),
                event.getDeviceName(),
                event.getDepositAmount(),
                event.getVnpayTxnRef()
        );
    }
}
