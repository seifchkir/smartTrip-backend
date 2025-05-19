package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Model.AuthenticationRequest;
import com.SmarTrip.smarTrip_backend.Security.AuthenticationResponse;
import com.SmarTrip.smarTrip_backend.Security.RegisterRequest;
import com.SmarTrip.smarTrip_backend.Service.AuthService;
import com.SmarTrip.smarTrip_backend.Service.CloudinaryService; // New import
// Removed FirebaseService import
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType; // Add this import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
// Removed ExecutionException import

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CloudinaryService cloudinaryService;
    // Removed FirebaseService field

    @GetMapping("/test-endpoint")
    public String test() {
        return "Server is working!";
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/upload-profile-picture/{userId}")
    public ResponseEntity<String> uploadProfilePicture(
            @PathVariable String userId,
            @RequestParam("photo") MultipartFile photo
    ) {
        try {
            System.out.println("Received upload request for user: " + userId);
            
            if (photo != null && !photo.isEmpty()) {
                // Upload to Cloudinary
                String imageUrl = cloudinaryService.uploadFile(photo);
                System.out.println("Image uploaded successfully: " + imageUrl);
                
                // Update user profile with the image URL
                authService.updateProfilePicture(userId, imageUrl);
                
                return ResponseEntity.ok(imageUrl);
            }
            return ResponseEntity.badRequest().body("No photo provided");
        } catch (Exception e) {
            System.err.println("Error in uploadProfilePicture: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading photo: " + e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody com.SmarTrip.smarTrip_backend.Security.AuthenticationRequest request
    ) {
        try {
            System.out.println("Authentication request received for email: " + request.getEmail());
            
            var modelRequest = new com.SmarTrip.smarTrip_backend.Model.AuthenticationRequest();
            modelRequest.setEmail(request.getEmail());
            modelRequest.setPassword(request.getPassword());

            AuthenticationResponse response = authService.authenticate(modelRequest);
            System.out.println("Authentication successful for user: " + request.getEmail());
            
            // Removed Firebase authentication storage
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            
            // Return more detailed error information
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Authentication failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new java.util.Date());
            
            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    // Add this new endpoint
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Check if the Authorization header starts with "Bearer "
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(400).body(Map.of(
                    "error", "Invalid Authorization header",
                    "message", "Authorization header must start with 'Bearer '"
                ));
            }
            
            // Extract token from the Authorization header (remove "Bearer " prefix)
            String jwtToken = authorizationHeader.substring(7);
            
            // Get user details from the token
            Map<String, Object> userProfile = authService.getUserProfileFromToken(jwtToken);
            return ResponseEntity.ok(userProfile);
        } catch (Exception e) {
            System.err.println("Error getting user profile from token: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body(Map.of(
                "error", "Unauthorized",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/test-upload")
    public ResponseEntity<String> testUpload(@RequestParam("file") MultipartFile file) {
        System.out.println("Test upload endpoint hit");
        if (file != null && !file.isEmpty()) {
            return ResponseEntity.ok("File received: " + file.getOriginalFilename());
        }
        return ResponseEntity.badRequest().body("No file provided");
    }

    @GetMapping("/test-simple")
    public ResponseEntity<String> testSimple() {
        System.out.println("Simple test endpoint accessed");
        return ResponseEntity.ok("Simple test endpoint works!");
    }

    // Removed Firebase user endpoint
}
