package com.SmarTrip.smarTrip_backend.Controller;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final Cloudinary cloudinary;

    @GetMapping("/cloudinary")
    public Map<String, Object> testCloudinaryConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            // Try to ping Cloudinary with an empty options map
            Map<String, Object> options = new HashMap<>();
            Map pingResult = cloudinary.api().ping(options);
            
            result.put("status", "success");
            result.put("message", "Connected to Cloudinary successfully");
            result.put("details", pingResult);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("message", "Failed to connect to Cloudinary");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}