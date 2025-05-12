package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final CloudinaryService cloudinaryService;

    @Autowired
    public FileUploadController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("File upload controller is working!");
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "photo", required = false) MultipartFile file) {
        try {
            System.out.println("File upload endpoint accessed");
            
            if (file == null) {
                System.out.println("File is null");
                return ResponseEntity.badRequest().body("No file provided");
            }
            
            System.out.println("File name: " + file.getOriginalFilename());
            System.out.println("File size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            
            if (!file.isEmpty()) {
                String imageUrl = cloudinaryService.uploadFile(file);
                System.out.println("Uploaded to Cloudinary: " + imageUrl);
                return ResponseEntity.ok(imageUrl);
            }
            
            return ResponseEntity.badRequest().body("Empty file provided");
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}