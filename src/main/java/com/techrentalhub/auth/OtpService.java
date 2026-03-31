package com.techrentalhub.auth;

import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

@Service
public class OtpService {

    @Data
    private static class OtpDetails {
        private String otp;
        private int attempts;
        private LocalDateTime expiry;

        public OtpDetails(String otp) {
            this.otp = otp;
            this.attempts = 0;
            this.expiry = LocalDateTime.now().plusMinutes(5); // Expire in 5 mins
        }
    }

    private final Map<String, OtpDetails> cache = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        Random rnd = new Random();
        String otp = String.format("%06d", rnd.nextInt(999999));
        cache.put(email, new OtpDetails(otp));
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        OtpDetails details = cache.get(email);
        if (details == null) {
            throw new RuntimeException("OTP không tồn tại hoặc đã bị hủy do quá thời gian.");
        }

        if (LocalDateTime.now().isAfter(details.getExpiry())) {
            cache.remove(email);
            throw new RuntimeException("Mã OTP đã hết hạn (quá 5 phút). Vui lòng yêu cầu gửi lại.");
        }

        if (details.getAttempts() >= 5) {
            cache.remove(email);
            throw new RuntimeException("Bạn đã nhập sai quá 5 lần! Tính năng bị khóa tạm thời. Vui lòng lấy mã mới.");
        }

        if (details.getOtp().equals(otp)) {
            cache.remove(email); // Xóa khỏi In-Memory Cache sau khi kích hoạt thành công.
            return true;
        } else {
            details.setAttempts(details.getAttempts() + 1);
            throw new RuntimeException("Mã OTP không chính xác. Bạn còn " + (5 - details.getAttempts()) + " lần thử.");
        }
    }
}
