package com.techrentalhub.review;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * POST /api/reviews/{orderId}
     * User gửi đánh giá cho đơn thuê (chỉ khi COMPLETED, 1 lần duy nhất).
     * Body: { "stars": 5, "comment": "Máy tốt, giao nhanh!" }
     */
    @PostMapping("/{orderId}")
    public ResponseEntity<?> submitReview(
            @PathVariable Long orderId,
            Authentication authentication,
            @Valid @RequestBody ReviewRequest request) {
        try {
            String email = authentication.getName();
            Review review = reviewService.submitReview(orderId, email, request);
            return ResponseEntity.ok(Map.of(
                    "message", "✓ Cảm ơn bạn đã đánh giá!",
                    "reviewId", review.getId(),
                    "stars", review.getStars()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/reviews/device/{deviceId}
     * Lấy tất cả review của 1 thiết bị (public, không cần auth).
     */
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<Review>> getDeviceReviews(@PathVariable Long deviceId) {
        return ResponseEntity.ok(reviewService.getDeviceReviews(deviceId));
    }
}
