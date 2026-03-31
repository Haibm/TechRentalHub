package com.techrentalhub.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderId(Long orderId);

    List<Review> findByDeviceIdOrderByCreatedAtDesc(Long deviceId);
}
