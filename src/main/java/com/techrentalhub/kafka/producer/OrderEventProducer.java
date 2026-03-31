package com.techrentalhub.kafka.producer;

import com.techrentalhub.kafka.KafkaTopicConfig;
import com.techrentalhub.kafka.event.OrderCreatedEvent;
import com.techrentalhub.kafka.event.PaymentConfirmedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {
        log.info("[KAFKA PRODUCER] Gửi sự kiện OrderCreated lên topic '{}' → Đơn #{}", 
                KafkaTopicConfig.ORDER_EVENTS_TOPIC, event.getOrderId());
        kafkaTemplate.send(KafkaTopicConfig.ORDER_EVENTS_TOPIC, 
                String.valueOf(event.getOrderId()), event);
    }

    public void publishPaymentConfirmed(PaymentConfirmedEvent event) {
        log.info("[KAFKA PRODUCER] Gửi sự kiện PaymentConfirmed lên topic '{}' → TxnRef: {}", 
                KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, event.getVnpayTxnRef());
        kafkaTemplate.send(KafkaTopicConfig.PAYMENT_EVENTS_TOPIC, 
                event.getVnpayTxnRef(), event);
    }
}
