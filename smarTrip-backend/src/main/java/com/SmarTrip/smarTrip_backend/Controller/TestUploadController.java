package com.SmarTrip.smarTrip_backend.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/test-uploads")
@CrossOrigin(origins = "*")
public class TestUploadController {

    @GetMapping
    public ResponseEntity<String> test() {
        System.out.println("Test uploads endpoint accessed");
        return ResponseEntity.ok("Test uploads endpoint works!");
    }
    
    @PostMapping(value = "/simple", consumes = "multipart/form-data")
    public ResponseEntity<String> simpleUpload(@RequestParam("file") MultipartFile file) {
        System.out.println("Simple upload endpoint accessed");
        System.out.println("File name: " + file.getOriginalFilename());
        System.out.println("File size: " + file.getSize());
        System.out.println("Content type: " + file.getContentType());
        
        if (file != null && !file.isEmpty()) {
            return ResponseEntity.ok("File received: " + file.getOriginalFilename());
        }
        return ResponseEntity.badRequest().body("No file provided");
    }
}