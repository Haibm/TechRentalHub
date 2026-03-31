package com.techrentalhub.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Khóa DB để check trùng lịch. 
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.device.id = :deviceId " +
           "AND o.status NOT IN (com.techrentalhub.order.OrderStatus.CANCELED, com.techrentalhub.order.OrderStatus.COMPLETED) " +
           "AND o.startDate <= :endDate AND o.endDate >= :startDate")
    List<Order> findOverlappingOrdersForUpdate(@Param("deviceId") Long deviceId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    Order findByVnpayTxnRef(String vnpayTxnRef);

    // Admin analytics queries
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.depositAmount) FROM Order o WHERE o.status = com.techrentalhub.order.OrderStatus.DEPOSIT_PAID OR o.status = com.techrentalhub.order.OrderStatus.RENTING OR o.status = com.techrentalhub.order.OrderStatus.COMPLETED")
    Double sumCollectedDeposits();

    @Query("SELECT DISTINCT o.device.id FROM Order o WHERE o.status IN (com.techrentalhub.order.OrderStatus.RENTING, com.techrentalhub.order.OrderStatus.DEPOSIT_PAID)")
    List<Long> findActiveDeviceIds();

    @Query(value = """
        SELECT o.device_id as deviceId, d.name as deviceName, d.category as category,
               COUNT(o.id) as orderCount, SUM(o.deposit_amount) as totalRevenue
        FROM orders o JOIN devices d ON o.device_id = d.id
        GROUP BY o.device_id, d.name, d.category
        ORDER BY orderCount DESC
        LIMIT 5
        """, nativeQuery = true)
    List<Object[]> findTop5DevicesByOrders();

    // Dashboard Trend Queries
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT DATE(created_at) as date, COUNT(*) as count, SUM(deposit_amount) as revenue
        FROM orders
        WHERE created_at >= :since
        GROUP BY DATE(created_at)
        ORDER BY date ASC
        """, nativeQuery = true)
    List<Object[]> getDailyStats(@org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT DATE_FORMAT(created_at, '%Y-%m') as month, COUNT(*) as count, SUM(deposit_amount) as revenue,
               SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) as completed
        FROM orders
        WHERE created_at >= :since
        GROUP BY month
        ORDER BY month ASC
        """, nativeQuery = true)
    List<Object[]> getMonthlyStats(@org.springframework.data.repository.query.Param("since") java.time.LocalDateTime since);

    // Staff KPI Queries
    long countByProcessedByAdminId(Long adminId);
    long countByProcessedByAdminIdAndStatus(Long adminId, com.techrentalhub.order.OrderStatus status);

    @org.springframework.data.jpa.repository.Query("SELECT AVG(TIMESTAMPDIFF(HOUR, o.createdAt, o.processedAt)) FROM Order o WHERE o.processedByAdminId = :adminId AND o.processedAt IS NOT NULL")
    Double getAvgProcessingTimeHours(@org.springframework.data.repository.query.Param("adminId") Long adminId);

    // User order history
    org.springframework.data.domain.Page<Order> findByUserEmailOrderByCreatedAtDesc(
        String email,
        org.springframework.data.domain.Pageable pageable
    );
}
