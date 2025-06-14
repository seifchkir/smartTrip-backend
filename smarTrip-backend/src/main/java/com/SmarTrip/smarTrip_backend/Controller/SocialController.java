package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Service.CloudinaryService;
import com.SmarTrip.smarTrip_backend.Service.FirebaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/social")
@RequiredArgsConstructor
public class SocialController {

    private final FirebaseService firebaseService;
    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/posts", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<?> createPost(
            @RequestParam("text") String text,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "tags", required = false) String tags
    ) {
        try {
            // Get current user info (replace with your actual authentication logic)
            String userId = getCurrentUserId();
            String userName = getCurrentUserName();
            String userPhoto = getCurrentUserPhoto();
            
            String postId = UUID.randomUUID().toString();
            
            // Create post data
            Map<String, Object> postData = new HashMap<>();
            postData.put("postId", postId);
            postData.put("userId", userId);
            postData.put("userName", userName);
            postData.put("userPhoto", userPhoto);
            postData.put("text", text);
            postData.put("createdAt", System.currentTimeMillis());
            postData.put("likes", 0);
            postData.put("comments", 0);
            
            if (title != null && !title.isEmpty()) {
                postData.put("title", title);
            }
            
            if (tags != null && !tags.isEmpty()) {
                // Split tags by comma and trim whitespace
                String[] tagArray = tags.split(",");
                for (int i = 0; i < tagArray.length; i++) {
                    tagArray[i] = tagArray[i].trim();
                }
                postData.put("tags", Arrays.asList(tagArray));
            }
            
            // Handle image upload if provided
            if (image != null && !image.isEmpty()) {
                try {
                    String imageUrl = cloudinaryService.uploadFile(image);
                    postData.put("imageUrl", imageUrl);
                } catch (Exception e) {
                    // Log error but continue without image
                    System.err.println("Error uploading image: " + e.getMessage());
                }
            }
            
            // Save to Firebase
            String updateTime = firebaseService.saveDocument("posts", postId, postData);
            
            Map<String, Object> response = new HashMap<>();
            response.put("postId", postId);
            response.put("updateTime", updateTime);
            response.put("data", postData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to create post",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/posts")
    public ResponseEntity<?> getAllPosts() {
        try {
            Map<String, Object> posts = firebaseService.getAllDocuments("posts");
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to retrieve posts",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/user/posts")
    public ResponseEntity<?> getUserPosts() {
        try {
            String userId = getCurrentUserId();
            Map<String, Object> userPosts = firebaseService.getDocumentsWhere("posts", "userId", userId);
            
            return ResponseEntity.ok(userPosts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to retrieve user posts",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> getPostsByUserId(@PathVariable String userId) {
        try {
            Map<String, Object> userPosts = firebaseService.getDocumentsWhere("posts", "userId", userId);
            
            return ResponseEntity.ok(userPosts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to retrieve posts for user",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/posts/{postId}")
    public ResponseEntity<?> getPost(@PathVariable String postId) {
        try {
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            
            if (post != null) {
                return ResponseEntity.ok(post);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to retrieve post",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable String postId) {
        try {
            String userId = getCurrentUserId();
            
            // Get current post
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user already liked this post
            Map<String, Object> existingLikes = firebaseService.getDocumentsWhere(
                "likes", 
                "postId", postId, 
                "userId", userId
            );
            
            if (existingLikes != null && !existingLikes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Already liked",
                    "message", "User has already liked this post"
                ));
            }
            
            // Create like record
            String likeId = UUID.randomUUID().toString();
            Map<String, Object> like = new HashMap<>();
            like.put("likeId", likeId);
            like.put("userId", userId);
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
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to like post",
                "message", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<?> unlikePost(@PathVariable String postId) {
        try {
            String userId = getCurrentUserId();
            
            // Get current post
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            if (post == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Find user's like for this post
            Map<String, Object> existingLikes = firebaseService.getDocumentsWhere(
                "likes", 
                "postId", postId, 
                "userId", userId
            );
            
            if (existingLikes == null || existingLikes.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Not liked",
                    "message", "User has not liked this post"
                ));
            }
            
            // Get the first like (there should only be one)
            String likeId = existingLikes.keySet().iterator().next();
            
            // Delete the like
            firebaseService.deleteDocument("likes", likeId);
            
            // Update post like count
            int currentLikes = ((Number) post.getOrDefault("likes", 0)).intValue();
            post.put("likes", Math.max(0, currentLikes - 1)); // Ensure we don't go below 0
            firebaseService.saveDocument("posts", postId, post);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "postId", postId,
                "likes", Math.max(0, currentLikes - 1)
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to unlike post",
                "message", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable String postId,
            @RequestBody Map<String, Object> commentData
    ) {
        try {
            // Get current user info
            String userId = getCurrentUserId();
            String userName = getCurrentUserName();
            String userPhoto = getCurrentUserPhoto();
            
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
            comment.put("userId", userId);
            comment.put("userName", userName);
            comment.put("userPhoto", userPhoto);
            comment.put("createdAt", System.currentTimeMillis());
            
            // Save comment to Firebase
            firebaseService.saveDocument("comments", commentId, comment);
            
            // Update post comment count
            int currentComments = ((Number) post.getOrDefault("comments", 0)).intValue();
            post.put("comments", currentComments + 1);
            firebaseService.saveDocument("posts", postId, post);
            
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to add comment",
                "message", e.getMessage()
            ));
        }
    }
    
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable String postId) {
        try {
            // Get comments for post
            Map<String, Object> comments = firebaseService.getDocumentsWhere("comments", "postId", postId);
            
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to get comments",
                "message", e.getMessage()
            ));
        }
    }
    
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String postId,
            @PathVariable String commentId
    ) {
        try {
            String userId = getCurrentUserId();
            
            // Get the comment
            Map<String, Object> comment = firebaseService.getDocument("comments", commentId);
            
            if (comment == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Check if user is the comment owner
            String commentUserId = (String) comment.get("userId");
            if (!userId.equals(commentUserId)) {
                return ResponseEntity.status(403).body(Map.of(
                    "error", "Forbidden",
                    "message", "You can only delete your own comments"
                ));
            }
            
            // Delete the comment
            firebaseService.deleteDocument("comments", commentId);
            
            // Update post comment count
            Map<String, Object> post = firebaseService.getDocument("posts", postId);
            if (post != null) {
                int currentComments = ((Number) post.getOrDefault("comments", 0)).intValue();
                post.put("comments", Math.max(0, currentComments - 1));
                firebaseService.saveDocument("posts", postId, post);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Failed to delete comment",
                "message", e.getMessage()
            ));
        }
    }
    
    // Helper methods for user authentication
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // This assumes your authentication principal contains the user ID
            // Adjust based on your actual authentication implementation
            return authentication.getName();
        }
        // Fallback for testing or when not authenticated
        return "test-user-123";
    }
    
    private String getCurrentUserName() {
        // For testing purposes - replace with actual user data
        return "Test User";
    }
    
    private String getCurrentUserPhoto() {
        // For testing purposes - replace with actual user data
        return "https://via.placeholder.com/150";
    }
}