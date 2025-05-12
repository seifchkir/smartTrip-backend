package com.SmarTrip.smarTrip_backend.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            System.out.println("Starting Cloudinary upload for file: " + file.getOriginalFilename());
            
            // Create a Map to hold upload parameters
            Map<String, Object> params = new HashMap<>();
            params.put("resource_type", "auto"); // Let Cloudinary detect the resource type
            
            // Upload the file
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
            
            System.out.println("Upload result: " + uploadResult);
            
            // Return the secure URL
            return uploadResult.get("secure_url").toString();
        } catch (Exception e) {
            System.err.println("Error in CloudinaryService.uploadFile: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}