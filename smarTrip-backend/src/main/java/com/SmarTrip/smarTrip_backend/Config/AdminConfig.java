package com.SmarTrip.smarTrip_backend.Config;

import com.SmarTrip.smarTrip_backend.Service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner createAdminAccount() {
        return args -> {
            try {
                // Check if admin account exists
                if (!authService.isAdminExists()) {
                    System.out.println("Creating admin account...");
                    
                    String adminPassword = "admin123"; // Change this to a secure password
                    
                    authService.createAdminAccountProgrammatically(
                        "admin@example.com",
                        adminPassword,
                        "Admin",
                        "User",
                        LocalDate.of(1990, 1, 1)
                    );
                    
                    System.out.println("Admin account created successfully!");
                } else {
                    System.out.println("Admin account already exists");
                }
            } catch (Exception e) {
                System.err.println("Error creating admin account: " + e.getMessage());
            }
        };
    }
}
