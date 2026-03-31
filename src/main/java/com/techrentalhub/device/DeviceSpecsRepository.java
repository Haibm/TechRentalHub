package com.techrentalhub.device;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface DeviceSpecsRepository extends MongoRepository<DeviceSpecs, String> {
    Optional<DeviceSpecs> findByDeviceId(Long deviceId);
}
