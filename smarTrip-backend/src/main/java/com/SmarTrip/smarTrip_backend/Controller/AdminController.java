package com.SmarTrip.smarTrip_backend.Controller;

import com.SmarTrip.smarTrip_backend.Service.AdminService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // Check if user has admin role
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // Age-based analytics
    @GetMapping("/analytics/age-groups")
    public Map<String, Integer> getUserCountByAgeGroup() {
        if (!isAdmin()) {
            throw new RuntimeException("Access denied");
        }
        return adminService.getUserCountByAgeGroup();
    }

    @GetMapping("/analytics/most-visited")
    public Map<String, String> getMostVisitedDestinationsByAgeGroup() {
        if (!isAdmin()) {
            throw new RuntimeException("Access denied");
        }
        return adminService.getMostVisitedDestinationsByAgeGroup();
    }

    // Content dashboard
    @GetMapping("/content/posts")
    public Map<String, Object> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String dateFilter,
            @RequestParam(required = false) String userFilter
    ) {
        if (!isAdmin()) {
            throw new RuntimeException("Access denied");
        }
        return adminService.getPosts(page, size, dateFilter, userFilter);
    }

    // Add more admin endpoints as needed
}
