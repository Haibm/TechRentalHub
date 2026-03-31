package com.techrentalhub.order;

public enum OrderStatus {
    PENDING_DEPOSIT,    // Vừa đặt kèo, chờ VNPAY
    DEPOSIT_PAID,       // Khách đóng cọc xong, khóa máy
    RENTING,            // Đã nhận máy
    COMPLETED,          // Đã trả máy & thanh toán phần còn lại
    CANCELED            // Hủy
}
