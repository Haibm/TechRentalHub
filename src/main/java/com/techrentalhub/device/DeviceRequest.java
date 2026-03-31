package com.techrentalhub.device;

import lombok.Data;
import java.util.Map;

@Data
public class DeviceRequest {
    private String name;
    private String brand;
    private Double basePrice;
    private Integer stockQuantity;
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private String display;
    private String os;
    private Double weightKg;
    private Map<String, Object> extraSpecs;
}
