package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Model.User;
import com.SmarTrip.smarTrip_backend.Service.CloudinaryService;
import com.SmarTrip.smarTrip_backend.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable String userId) {
        try {
            System.out.println("Getting user profile for ID: " + userId);
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            System.err.println("Error getting user profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    // New simplified endpoint that accepts email as a form parameter
    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(
            @RequestParam("email") String email,
            @RequestParam("profilePicture") MultipartFile profilePicture) {
        try {
            System.out.println("Uploading profile picture for user email: " + email);
            
            if (profilePicture == null || profilePicture.isEmpty()) {
                return ResponseEntity.badRequest().body("No profile picture provided");
            }
            
            // Upload to Cloudinary
            String imageUrl = cloudinaryService.uploadFile(profilePicture);
            
            // Update user profile with the image URL using email
            userService.updateProfilePictureByEmail(email, imageUrl);
            
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            System.err.println("Error uploading profile picture: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error uploading profile picture: " + e.getMessage());
        }
    }
    
    @PostMapping("/complete-profile")
    public ResponseEntity<?> completeProfile(
            @RequestParam("email") String email,
            @RequestParam(value = "profilePicture", required = false) MultipartFile profilePicture) {
        try {
            System.out.println("Completing profile for user email: " + email);
            
            // Find the user by email
            User user = userService.getUserByEmail(email);
            
            // If a profile picture was provided, upload it
            if (profilePicture != null && !profilePicture.isEmpty()) {
                System.out.println("Uploading profile picture: " + profilePicture.getOriginalFilename());
                String imageUrl = cloudinaryService.uploadFile(profilePicture);
                user.setPhotoUrl(imageUrl);
                userService.saveUser(user);
                return ResponseEntity.ok().body(Map.of(
                    "message", "Profile completed successfully",
                    "photoUrl", imageUrl,
                    "userId", user.getId()
                ));
            }
            
            // If no profile picture was provided (user clicked "Skip for now")
            return ResponseEntity.ok().body(Map.of(
                "message", "Profile completed without photo",
                "userId", user.getId()
            ));
        } catch (Exception e) {
            System.err.println("Error completing profile: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to complete profile: " + e.getMessage()
            ));
        }
    }
    
    // Add an endpoint to get user by email
    @GetMapping("/by-email")
    public ResponseEntity<?> getUserByEmail(@RequestParam String email) {
        try {
            System.out.println("Fetching user profile for email: " + email);
            User user = userService.getUserByEmail(email);
            
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("firstName", user.getFirstName());
            userDetails.put("lastName", user.getLastName());
            userDetails.put("email", user.getEmail());
            userDetails.put("photoUrl", user.getPhotoUrl());
            // Removed the createdAt field since it doesn't exist in your User model
            
            return ResponseEntity.ok(userDetails);
        } catch (Exception e) {
            System.err.println("Error fetching user by email: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(404).body(Map.of(
                "error", "User not found",
                "message", e.getMessage()
            ));
        }
    }
}