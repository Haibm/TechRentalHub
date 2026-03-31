package com.techrentalhub.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "device_specs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceSpecs {
    
    @Id
    private String id;
    
    private Long deviceId; // Ref to MySQL Device
    
    private String cpu;
    private String ram;
    private String storage;
    private String gpu;
    private String display;
    private String os;
    private Double weightKg;
    
    // Thuộc tính động hỗ trợ nhiều loại thiết kế (máy ảnh thẻ nhớ v.v.)
    private Map<String, Object> extraSpecs;
}
