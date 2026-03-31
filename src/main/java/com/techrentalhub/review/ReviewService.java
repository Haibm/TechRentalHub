package com.techrentalhub.review;

import com.techrentalhub.order.Order;
import com.techrentalhub.order.OrderRepository;
import com.techrentalhub.order.OrderStatus;
import com.techrentalhub.user.User;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public Review submitReview(Long orderId, String userEmail, ReviewRequest request) {
        // 1. Kiểm tra đơn hàng tồn tại
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng #" + orderId));

        // 2. Chỉ đơn COMPLETED mới được review
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá sau khi đơn thuê đã HOÀN THÀNH (COMPLETED). Trạng thái hiện tại: " + order.getStatus());
        }

        // 3. Kiểm tra đây là đơn của chính user đang đăng nhập
        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này.");
        }

        // 4. Chặn review lần 2
        if (reviewRepository.existsByOrderId(orderId)) {
            throw new RuntimeException("Bạn đã đánh giá đơn hàng này rồi. Mỗi đơn chỉ được đánh giá 1 lần.");
        }

        // 5. Lưu review
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));

        Review review = Review.builder()
                .order(order)
                .deviceId(order.getDevice().getId())
                .userId(user.getId())
                .stars(request.getStars())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }

    public List<Review> getDeviceReviews(Long deviceId) {
        return reviewRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }
}
