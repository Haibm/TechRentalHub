package com.techrentalhub.order;

import com.techrentalhub.device.Device;
import com.techrentalhub.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Integer rentalDays;
    
    // Tổng số tiền hóa đơn
    private Double totalAmount;
    
    // Tiền cọc (Tính = 50% * Giá gốc máy)
    private Double depositAmount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String vnpayTxnRef; // Mã giao dịch VNPAY mapping ngược

    // Late return tracking
    private LocalDate actualReturnDate;  // Ngày thực tế trả máy
    private Double lateFee;              // Phụ phí trả trễ (nếu có)

    // Staff KPI tracking
    private Long processedByAdminId;     // ID admin xử lý đơn (mark-renting/complete/cancel)
    private LocalDateTime processedAt;  // Thời điểm admin xử lý lần đầu tiên

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
