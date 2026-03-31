package com.techrentalhub.order;

import com.techrentalhub.device.Device;
import com.techrentalhub.device.DeviceRepository;
import com.techrentalhub.kafka.event.OrderCreatedEvent;
import com.techrentalhub.kafka.event.PaymentConfirmedEvent;
import com.techrentalhub.kafka.producer.OrderEventProducer;
import com.techrentalhub.user.User;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final VnPayService vnPayService;
    private final PdfExportService pdfExportService;
    private final OrderEventProducer orderEventProducer; // Kafka Producer
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate; // WebSocket Template

    @Transactional
    public String createOrderAndGetPaymentUrl(String email, OrderRequest request, String ipAddress) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new RuntimeException("Device not found"));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new RuntimeException("Ngày trả máy phải diễn ra sau ngày mượn.");
        }

        // 1. CHỐNG OVERBOOKING: Sử dụng Query Pessimistic Lock
        List<Order> overlaps = orderRepository.findOverlappingOrdersForUpdate(device.getId(), request.getStartDate(),
                request.getEndDate());
        if (!overlaps.isEmpty()) {
            throw new RuntimeException("Rất tiếc! Thiết bị này đã có người giữ lịch trong khoảng thời gian trên.");
        }

        // 2. Công thức Tiền Cọc: 50% * Giá trị gốc chiếc máy
        int days = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        if (days == 0) days = 1;

        Double totalAmount = device.getBasePrice() * days;
        Double depositAmount = device.getBasePrice() * 0.5;

        String txnRef = com.techrentalhub.core.config.VnPayConfig.getRandomNumber(8);

        Order order = Order.builder()
                .user(user)
                .device(device)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .rentalDays(days)
                .totalAmount(totalAmount)
                .depositAmount(depositAmount)
                .status(OrderStatus.PENDING_DEPOSIT)
                .vnpayTxnRef(txnRef)
                .build();

        orderRepository.save(order);

        // 3. [KAFKA] Publish event bất đồng bộ – Consumer sẽ gửi Email xác nhận cho khách
        orderEventProducer.publishOrderCreated(new OrderCreatedEvent(
                order.getId(),
                user.getEmail(),
                user.getFullName() != null ? user.getFullName() : user.getUsername(),
                device.getName(),
                request.getStartDate(),
                request.getEndDate(),
                depositAmount,
                txnRef
        ));

        // 4. Return lại Link Sandbox VNPay để khách thanh toán
        return vnPayService.createPaymentUrl(depositAmount.longValue(), txnRef, ipAddress);
    }

    @Transactional
    public void confirmPayment(String txnRef) {
        Order order = orderRepository.findByVnpayTxnRef(txnRef);
        if (order != null && order.getStatus() == OrderStatus.PENDING_DEPOSIT) {
            order.setStatus(OrderStatus.DEPOSIT_PAID);
            orderRepository.save(order);

            // [KAFKA] Publish event thanh toán thành công – Consumer gửi Email receipt
            orderEventProducer.publishPaymentConfirmed(new PaymentConfirmedEvent(
                    order.getId(),
                    order.getUser().getEmail(),
                    order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getUsername(),
                    order.getDevice().getName(),
                    txnRef,
                    order.getDepositAmount()
            ));

            // [WEBSOCKET] Đẩy thông báo thời gian thực cho Staff Dashboard
            messagingTemplate.convertAndSend("/topic/orders", com.techrentalhub.order.dto.OrderNotificationDto.builder()
                    .orderId(order.getId())
                    .customerName(order.getUser().getFullName() != null ? order.getUser().getFullName() : order.getUser().getUsername())
                    .deviceName(order.getDevice().getName())
                    .depositAmount(order.getDepositAmount())
                    .message("🔔 Có đơn hàng mới đã thanh toán!")
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build());

        } else if (order != null && order.getStatus() == OrderStatus.DEPOSIT_PAID) {
            throw new RuntimeException("Phát hiện truy cập bất thường (Replay Attack): Lệnh thanh toán /vnpay-return bị cố ý gọi đè nhiều lần. Máy chủ hiện đã ngăn chặn thành công.");
        }
    }

    public boolean verifyVnPaySignature(jakarta.servlet.http.HttpServletRequest request) {
        return vnPayService.verifySignature(request);
    }

    public byte[] printContract(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.PENDING_DEPOSIT || order.getStatus() == OrderStatus.CANCELED) {
            throw new RuntimeException("Chưa hoàn tất thanh toán tiền cọc, không thể in hợp đồng!");
        }
        return pdfExportService.exportContract(order);
    }
}
