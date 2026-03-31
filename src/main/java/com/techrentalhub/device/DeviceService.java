package com.techrentalhub.device;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceSpecsRepository deviceSpecsRepository;
    private final DeviceDocumentRepository deviceDocumentRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public Device createDevice(DeviceRequest request, MultipartFile image) throws IOException {
        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = cloudinaryService.uploadImage(image);
        }

        // 1. MySQL
        Device device = Device.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .basePrice(request.getBasePrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(imageUrl)
                .build();
        device = deviceRepository.save(device);

        // 2. MongoDB
        DeviceSpecs specs = DeviceSpecs.builder()
                .deviceId(device.getId())
                .cpu(request.getCpu())
                .ram(request.getRam())
                .storage(request.getStorage())
                .gpu(request.getGpu())
                .display(request.getDisplay())
                .os(request.getOs())
                .weightKg(request.getWeightKg())
                .extraSpecs(request.getExtraSpecs())
                .build();
        deviceSpecsRepository.save(specs);

        // 3. Elasticsearch
        DeviceDocument esDoc = DeviceDocument.builder()
                .id(device.getId().toString())
                .name(device.getName())
                .brand(device.getBrand())
                .basePrice(device.getBasePrice())
                .imageUrl(device.getImageUrl())
                .cpu(specs.getCpu())
                .ram(specs.getRam())
                .extraSpecs(specs.getExtraSpecs())
                .build();
        deviceDocumentRepository.save(esDoc);

        return device;
    }

    public Iterable<DeviceDocument> searchDevices(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return deviceDocumentRepository.findAll();
        }
        return deviceDocumentRepository.findByNameContainingOrBrandContaining(keyword, keyword);
    }
}
