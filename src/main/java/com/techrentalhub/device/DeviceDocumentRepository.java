package com.techrentalhub.device;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface DeviceDocumentRepository extends ElasticsearchRepository<DeviceDocument, String> {
    List<DeviceDocument> findByNameContainingOrBrandContaining(String name, String brand);
}
