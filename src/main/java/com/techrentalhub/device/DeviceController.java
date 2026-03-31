package com.techrentalhub.device;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Device> addDevice(
            @RequestPart("data") DeviceRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        return ResponseEntity.ok(deviceService.createDevice(request, image));
    }

    @GetMapping("/search")
    public ResponseEntity<Iterable<DeviceDocument>> search(
            @RequestParam(value = "q", required = false) String query
    ) {
        return ResponseEntity.ok(deviceService.searchDevices(query));
    }
}
