package com.techrentalhub.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendOtpEmail(String toAddress, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toAddress);
        message.setSubject("TechRentalHub - Mã Xác Nhận OTP Chống Spam");
        message.setText("Chào bạn,\n\nMã xác nhận OTP (6 số) của bạn là: " + otp + 
                        "\n\nMã có hiệu lực trong 5 phút. Vui lòng không chia sẻ mã này với người lạ.\nBạn có tối đa 5 lần nhập sai mã trước khi bị khóa tạm thời.\n\nĐội ngũ Security TechRentalHub.");
        javaMailSender.send(message);
    }

    // Generic method - dùng bởi Kafka Consumer để gửi email thông báo đơn hàng/thanh toán
    public void sendEmail(String toAddress, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toAddress);
        message.setSubject(subject);
        message.setText(body);
        javaMailSender.send(message);
    }
}
