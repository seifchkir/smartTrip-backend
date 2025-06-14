package com.SmarTrip.smarTrip_backend.Service;

import com.SmarTrip.smarTrip_backend.Model.User;
import com.SmarTrip.smarTrip_backend.Repositry.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FirebaseService firebaseService;

    // Age groups for analytics
    private static final List<String> AGE_GROUPS = Arrays.asList(
        "<18",
        "18-25",
        "26-35",
        "36-50",
        "50+"
    );

    // Get user count by age group
    public Map<String, Integer> getUserCountByAgeGroup() {
        List<User> users = userRepository.findAll();
        Map<String, Integer> ageGroupCounts = new HashMap<>();
        
        // Initialize all age groups with 0
        AGE_GROUPS.forEach(ageGroup -> ageGroupCounts.put(ageGroup, 0));
        
        // Calculate age group counts
        users.forEach(user -> {
            int age = calculateUserAge(user); // You'll need to implement this based on your User model
            String ageGroup = determineAgeGroup(age);
            ageGroupCounts.put(ageGroup, ageGroupCounts.get(ageGroup) + 1);
        });
        
        return ageGroupCounts;
    }

    // Get most visited destination by age group
    public Map<String, String> getMostVisitedDestinationsByAgeGroup() {
        Map<String, String> mostVisitedDestinations = new HashMap<>();
        
        // Query posts from Firebase
        try {
            Map<String, Object> posts = firebaseService.getAllDocuments("posts");
            
            // Process posts to find most visited destinations by age group
            // You'll need to implement this based on your post data structure
            // This is a placeholder implementation
            AGE_GROUPS.forEach(ageGroup -> {
                mostVisitedDestinations.put(ageGroup, "Popular Destination");
            });
            
            return mostVisitedDestinations;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching posts", e);
        }
    }

    // Get paginated posts with filters
    public Map<String, Object> getPosts(int page, int size, String dateFilter, String userFilter) {
        try {
            // Query posts from Firebase with filters
            Map<String, Object> posts = firebaseService.getAllDocuments("posts");
            
            // Convert and filter posts
            List<Map<String, Object>> filteredPosts = new ArrayList<>();
            for (Object postObj : posts.values()) {
                if (postObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> post = (Map<String, Object>) postObj;
                    
                    // Apply filters
                    boolean dateMatch = dateFilter == null || 
                        ((Long)post.get("createdAt") >= Long.parseLong(dateFilter));
                    boolean userMatch = userFilter == null || 
                        userFilter.equals(post.get("userId"));
                    
                    if (dateMatch && userMatch) {
                        filteredPosts.add(post);
                    }
                }
            }
            
            // Paginate results
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, filteredPosts.size());
            List<Map<String, Object>> pagePosts = filteredPosts.subList(fromIndex, toIndex);
            
            Map<String, Object> response = new HashMap<>();
            response.put("total", filteredPosts.size());
            response.put("page", page);
            response.put("size", size);
            response.put("posts", pagePosts);
            
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching posts", e);
        }
    }

    // Helper method to calculate user age
    private int calculateUserAge(User user) {
        // Implement based on your User model
        // This is a placeholder
        return 30; // Default age
    }

    // Helper method to determine age group
    private String determineAgeGroup(int age) {
        if (age < 18) return "<18";
        if (age <= 25) return "18-25";
        if (age <= 35) return "26-35";
        if (age <= 50) return "36-50";
        return "50+";
    }
}
