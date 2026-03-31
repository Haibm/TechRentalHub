package com.techrentalhub.ai;

import com.techrentalhub.admin.AdminService;
import com.techrentalhub.admin.dto.StatsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCopilotService {

    private final ChatClient chatClient;
    private final AdminService adminService;

    // In-memory conversation history per admin email
    // Production: nên dùng Redis
    private final Map<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    public String ask(String question, String adminEmail) {
        // 1. Lấy dữ liệu KPI thực từ DB
        StatsResponse stats = adminService.getStats();
        String dbContext = buildDbContext(stats);

        // 2. Lấy hoặc tạo chat history
        List<Message> history = conversationHistory.computeIfAbsent(adminEmail, k -> new ArrayList<>());

        // 3. Giới hạn lịch sử 20 tin nhắn gần nhất để tránh vượt token
        if (history.size() > 40) {
            history = new ArrayList<>(history.subList(history.size() - 40, history.size()));
            conversationHistory.put(adminEmail, history);
        }

        // 4. Tạo System Prompt với context DB
        String systemPrompt = buildSystemPrompt(dbContext, adminEmail);

        try {
            // 5. Gọi Spring AI ChatClient
            String answer = chatClient.prompt()
                    .system(systemPrompt)
                    .messages(history)
                    .user(question)
                    .call()
                    .content();

            // 6. Lưu conversation vào history
            history.add(new UserMessage(question));
            history.add(new AssistantMessage(answer));

            log.info("[COPILOT] Admin '{}' hỏi: {} | Đã trả lời.", adminEmail, question);
            return answer;

        } catch (Exception e) {
            log.error("[COPILOT] Lỗi Spring AI: {}", e.getMessage());
            return "⚠️ Không thể kết nối AI lúc này. Lỗi: " + e.getMessage();
        }
    }

    public void clearHistory(String adminEmail) {
        conversationHistory.remove(adminEmail);
    }

    private String buildSystemPrompt(String dbContext, String adminEmail) {
        return """
                Bạn là **TechRentalHub Copilot** – Trợ lý AI thông minh dành riêng cho Admin.
                Admin email hiện tại: %s
                
                NHIỆM VỤ:
                - Phân tích dữ liệu kinh doanh và trả lời chính xác bằng tiếng Việt
                - Gợi ý hành động cụ thể dựa trên số liệu (ví dụ: thiết bị nào nên nhập thêm, doanh thu tháng này thế nào)
                - Nếu câu hỏi không liên quan đến dữ liệu được cung cấp, hãy nói rõ là không có đủ thông tin
                - Luôn trình bày rõ ràng, có số liệu cụ thể, dùng bullet point nếu phù hợp
                
                NGUYÊN TẮC:
                - Chỉ sử dụng dữ liệu được cung cấp bên dưới, không bịa số liệu
                - Nếu dữ liệu là 0, hãy phân tích nguyên nhân có thể và đề xuất giải pháp
                - Trả lời ngắn gọn nhưng đầy đủ, không quá 300 từ mỗi câu
                
                ═══════════════════════════════════════
                📊 DỮ LIỆU KINH DOANH THỰC TẾ
                ═══════════════════════════════════════
                %s
                ═══════════════════════════════════════
                """.formatted(adminEmail, dbContext);
    }

    private String buildDbContext(StatsResponse stats) {
        return """
                📦 ĐƠN HÀNG:
                  • Tổng số đơn thuê: %d đơn
                  • Đơn chờ thanh toán cọc (PENDING): %d
                  • Đơn đã đặt cọc (DEPOSIT_PAID): %d
                  • Đơn đã hoàn thành (COMPLETED): %d
                  • Tỷ lệ hoàn thành: %.1f%%
                
                💰 DOANH THU:
                  • Tổng tiền cọc đã thu: %,.0f VNĐ
                  • TB tiền cọc/đơn: %,.0f VNĐ
                
                🖥️ THIẾT BỊ & KHÁCH HÀNG:
                  • Số thiết bị đang hoạt động (đang thuê/chờ nhận): %d chiếc
                  • Tổng tài khoản người dùng: %d người
                """.formatted(
                stats.getTotalOrders(),
                stats.getPendingOrders(),
                stats.getDepositPaidOrders(),
                stats.getCompletedOrders(),
                stats.getTotalOrders() > 0 ? (double) stats.getCompletedOrders() / stats.getTotalOrders() * 100 : 0.0,
                stats.getTotalRevenue(),
                stats.getTotalOrders() > 0 ? stats.getTotalRevenue() / stats.getTotalOrders() : 0.0,
                stats.getActiveDevices(),
                stats.getTotalUsers()
        );
    }
}
