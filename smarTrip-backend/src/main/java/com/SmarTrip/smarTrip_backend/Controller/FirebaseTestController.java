package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Service.CloudinaryService;
import com.SmarTrip.smarTrip_backend.Service.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/firebase-test")
@RequiredArgsConstructor
public class FirebaseTestController {

    private final FirebaseService firebaseService;
    private final CloudinaryService cloudinaryService;

    @PostMapping("/create")
    public ResponseEntity<?> createDocument(@RequestBody Map<String, Object> data) {
        try {
            String collection = "test_documents";
            String documentId = UUID.randomUUID().toString();
            
            // Add timestamp
            data.put("createdAt", System.currentTimeMillis());
            
            String updateTime = firebaseService.saveDocument(collection, documentId, data);
            
            Map<String, Object> response = new HashMap<>();
            response.put("documentId", documentId);
            response.put("updateTime", updateTime);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to create document",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/read/{documentId}")
    public ResponseEntity<?> readDocument(@PathVariable String documentId) {
        try {
            String collection = "test_documents";
            Map<String, Object> data = firebaseService.getDocument(collection, documentId);
            
            if (data != null) {
                return ResponseEntity.ok(data);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to read document",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/read-all")
    public ResponseEntity<?> readAllDocuments() {
        try {
            String collection = "test_documents";
            Map<String, Object> documents = firebaseService.getAllDocuments(collection);
            
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to read all documents",
                "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete/{documentId}")
    public ResponseEntity<?> deleteDocument(@PathVariable String documentId) {
        try {
            String collection = "test_documents";
            String updateTime = firebaseService.deleteDocument(collection, documentId);
            
            return ResponseEntity.ok(Map.of(
                "documentId", documentId,
                "deleteTime", updateTime
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to delete document",
                "message", e.getMessage()
            ));
        }
    }

    @PostMapping(value = "/create-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPostWithImage(
            @RequestParam("text") String text,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) String tags
    ) {
        try {
            System.out.println("Starting post creation process...");
            String collection = "posts";
            String postId = UUID.randomUUID().toString();
            
            // Create post data
            Map<String, Object> postData = new HashMap<>();
            postData.put("postId", postId);
            postData.put("text", text);
            postData.put("createdAt", System.currentTimeMillis());
            
            // Try to save a simple test document first to isolate the issue
            try {
                Map<String, Object> testData = new HashMap<>();
                testData.put("test", "value");
                testData.put("timestamp", System.currentTimeMillis());
                
                System.out.println("Testing Firebase connection with simple document...");
                String testResult = firebaseService.saveDocument("test_collection", "test_" + System.currentTimeMillis(), testData);
                System.out.println("Test document saved successfully: " + testResult);
            } catch (Exception e) {
                System.err.println("Firebase connection test failed: " + e.getMessage());
                System.err.println("Full stack trace:");
                e.printStackTrace();
                
                // Return detailed error about Firebase connection
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Firebase connection test failed");
                errorResponse.put("message", e.getMessage());
                errorResponse.put("type", e.getClass().getName());
                if (e.getCause() != null) {
                    errorResponse.put("cause", e.getCause().getMessage());
                    errorResponse.put("causeType", e.getCause().getClass().getName());
                }
                return ResponseEntity.status(500).body(errorResponse);
            }
            
            // Continue with the rest of the post creation
            if (title != null && !title.isEmpty()) {
                postData.put("title", title);
            }
            
            if (tags != null && !tags.isEmpty()) {
                // Split tags by comma and trim whitespace
                String[] tagArray = tags.split(",");
                for (int i = 0; i < tagArray.length; i++) {
                    tagArray[i] = tagArray[i].trim();
                }
                postData.put("tags", tagArray);
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                try {
                    // Upload to Cloudinary
                    System.out.println("Uploading image to Cloudinary...");
                    String imageUrl = cloudinaryService.uploadFile(image);
                    postData.put("imageUrl", imageUrl);
                    System.out.println("Image uploaded successfully: " + imageUrl);
                } catch (Exception e) {
                    System.err.println("Error uploading image to Cloudinary: " + e.getMessage());
                    // Continue without image if Cloudinary upload fails
                }
            }
            
            // Save to Firebase
            System.out.println("Saving post data to Firebase collection: " + collection);
            System.out.println("Post data: " + postData);
            
            try {
                String updateTime = firebaseService.saveDocument(collection, postId, postData);
                System.out.println("Post saved successfully with update time: " + updateTime);
                
                Map<String, Object> response = new HashMap<>();
                response.put("postId", postId);
                response.put("updateTime", updateTime);
                response.put("data", postData);
                
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                System.err.println("Firebase error details: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Caused by: " + e.getCause().getMessage());
                }
                throw e; // Re-throw to be caught by outer catch block
            }
        } catch (Exception e) {
            System.err.println("Error creating post: " + e.getMessage());
            e.printStackTrace();
            
            // More detailed error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create post");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            // Add cause if available
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
                errorResponse.put("causeType", e.getCause().getClass().getName());
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/test-firebase-connection")
    public ResponseEntity<?> testFirebaseConnection() {
        try {
            System.out.println("Testing Firebase connection...");
            
            // Create a simple test document
            Map<String, Object> testData = new HashMap<>();
            testData.put("test", "value");
            testData.put("timestamp", System.currentTimeMillis());
            
            String testId = "connection_test_" + System.currentTimeMillis();
            String result = firebaseService.saveDocument("connection_tests", testId, testData);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Firebase connection successful",
                "testId", testId,
                "result", result
            ));
        } catch (Exception e) {
            System.err.println("Firebase connection test failed: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "failed");
            errorResponse.put("error", "Firebase connection test failed");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
                errorResponse.put("causeType", e.getCause().getClass().getName());
            }
            
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable String postId) {
        try {
            System.out.println("Processing like for post: " + postId);
            
            // Get current post
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Create like record
            String likeId = UUID.randomUUID().toString();
            Map<String, Object> like = new HashMap<>();
            like.put("userId", "test-user-" + System.currentTimeMillis()); // For testing only
            like.put("postId", postId);
            like.put("createdAt", System.currentTimeMillis());
            
            // Save like to Firebase
            firebaseService.saveDocument("likes", likeId, like);
            
            // Update post like count
            int currentLikes = ((Number) post.getOrDefault("likes", 0)).intValue();
            post.put("likes", currentLikes + 1);
            firebaseService.saveDocument("posts", postId, post);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "likeId", likeId,
                "postId", postId,
                "likes", currentLikes + 1
            ));
        } catch (Exception e) {
            System.err.println("Error liking post: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to like post");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
                errorResponse.put("causeType", e.getCause().getClass().getName());
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String postId,
            @RequestBody Map<String, Object> commentData
    ) {
        try {
            System.out.println("Adding comment to post: " + postId);
            
            // Get current post
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Create comment object
            String commentId = UUID.randomUUID().toString();
            Map<String, Object> comment = new HashMap<>(commentData);
            comment.put("commentId", commentId);
            comment.put("postId", postId);
            comment.put("userId", "test-user-" + System.currentTimeMillis()); // For testing only
            comment.put("userName", "Test User");
            comment.put("userPhoto", "https://via.placeholder.com/150");
            comment.put("createdAt", System.currentTimeMillis());
            
            // Save comment to Firebase
            firebaseService.saveDocument("comments", commentId, comment);
            
            // Update post comment count
            int currentComments = ((Number) post.getOrDefault("comments", 0)).intValue();
            post.put("comments", currentComments + 1);
            firebaseService.saveDocument("posts", postId, post);
            
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            System.err.println("Error adding comment: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to add comment");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
                errorResponse.put("causeType", e.getCause().getClass().getName());
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable String postId) {
        try {
            System.out.println("Getting comments for post: " + postId);
            
            // Get comments for post
            Map<String, Object> comments = firebaseService.getDocumentsWhere("comments", "postId", postId);
            
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            System.err.println("Error getting comments: " + e.getMessage());
            e.printStackTrace();
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to get comments");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("type", e.getClass().getName());
            
            if (e.getCause() != null) {
                errorResponse.put("cause", e.getCause().getMessage());
                errorResponse.put("causeType", e.getCause().getClass().getName());
            }
            
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}