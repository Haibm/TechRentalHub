package com.techrentalhub.order;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> createOrder(
            Authentication authentication,
            @RequestBody OrderRequest request,
            HttpServletRequest httpRequest) {
        
        String email = authentication.getName(); 
        String ipAddress = com.techrentalhub.core.config.VnPayConfig.getIpAddress(httpRequest);
        
        String paymentUrl = orderService.createOrderAndGetPaymentUrl(email, request, ipAddress);
        return ResponseEntity.ok(paymentUrl);
    }

    // VNPAY gọi callback xác thực Signature
    @GetMapping("/vnpay-return")
    public ResponseEntity<String> vnpayReturn(HttpServletRequest request) {
        
        // 1. Chống chữ ký giả (Spoof Hash Signature Attack)
        if (!orderService.verifyVnPaySignature(request)) {
            return ResponseEntity.badRequest().body("Phát hiện lập trình viên nhái vnp_SecureHash. Yêu cầu giao dịch bị từ chối!");
        }

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        // 2. Chống Replay Attack trong Service
        if ("00".equals(responseCode)) {
            try {
                orderService.confirmPayment(txnRef);
                return ResponseEntity.ok("Thanh toán thành công! Mã Đơn của bạn là: " + txnRef);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        } else {
            return ResponseEntity.badRequest().body("Giao dịch thất bại hoặc đã bị khách hàng chủ động hủy.");
        }
    }

    // Nhấn vào in file Hợp đồng PDF
    @GetMapping("/{id}/contract.pdf")
    public ResponseEntity<byte[]> downloadContract(@PathVariable Long id) {
        byte[] pdfBytes = orderService.printContract(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=contract_techrentalhub_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
