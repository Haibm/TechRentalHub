package com.techrentalhub.device;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@Document(indexName = "devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceDocument {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Double)
    private Double basePrice;

    @Field(type = FieldType.Text)
    private String imageUrl;

    @Field(type = FieldType.Keyword)
    private String cpu;

    @Field(type = FieldType.Keyword)
    private String ram;

    @Field(type = FieldType.Object)
    private Map<String, Object> extraSpecs;
}
