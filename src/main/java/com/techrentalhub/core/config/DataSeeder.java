package com.techrentalhub.core.config;

import com.github.javafaker.Faker;
import com.techrentalhub.device.DeviceRequest;
import com.techrentalhub.device.DeviceService;
import com.techrentalhub.user.Role;
import com.techrentalhub.user.User;
import com.techrentalhub.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DeviceService deviceService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seeder.enabled:false}")
    private boolean seederEnabled;

    @Override
    public void run(String... args) throws Exception {
        // Luôn tạo Admin account mặc định khi khởi động (nếu chưa tồn tại)
        seedAdminAccount();

        if (!seederEnabled) return;

        System.out.println("Bắt đầu Data Seeder quá trình (100 thiết bị giả)...");
        Faker faker = new Faker();
        String[] brands = {"Dell", "HP", "Apple", "Lenovo", "Asus", "Acer"};
        String[] cpus = {"Intel Core i5", "Intel Core i7", "Intel Core i9", "AMD Ryzen 5", "AMD Ryzen 7", "Apple M1", "Apple M2"};
        String[] rams = {"8GB", "16GB", "32GB", "64GB"};

        for (int i = 0; i < 100; i++) {
            DeviceRequest req = new DeviceRequest();
            req.setName(faker.commerce().productName() + " Laptop");
            req.setBrand(brands[faker.random().nextInt(brands.length)]);
            double priceStr = faker.number().randomDouble(2, 500, 3000);
            req.setBasePrice(priceStr);
            req.setStockQuantity(faker.number().numberBetween(1, 50));
            req.setCpu(cpus[faker.random().nextInt(cpus.length)]);
            req.setRam(rams[faker.random().nextInt(rams.length)]);
            req.setStorage(faker.random().nextInt(256, 2048) + "GB SSD");
            req.setGpu("Integrated Graphics");
            req.setDisplay(faker.random().nextInt(13, 17) + " inch FHD");
            req.setOs("Windows / macOS");
            req.setWeightKg(faker.number().randomDouble(2, 1, 3));
            req.setExtraSpecs(new HashMap<>());

            try {
                deviceService.createDevice(req, null);
            } catch (Exception e) {
                System.out.println("Lỗi 1 record: " + e.getMessage());
            }
        }
        System.out.println("Hoàn thành Data Seeder sinh 100 records!");
    }

    private void seedAdminAccount() {
        if (userRepository.existsByUsername("admin")) {
            System.out.println("[DataSeeder] Admin account đã tồn tại, bỏ qua tạo mới.");
            return;
        }

        User admin = User.builder()
                .username("admin")
                .email("admin@techrentalhub.com")
                .password(passwordEncoder.encode("Admin@123"))
                .fullName("System Administrator")
                .role(Role.ROLE_ADMIN)
                .enabled(true) // Admin được kích hoạt ngay, không cần OTP
                .build();

        userRepository.save(admin);
        System.out.println("[DataSeeder] ✓ Đã tạo Admin account mặc định: admin / Admin@123");
    }
}
