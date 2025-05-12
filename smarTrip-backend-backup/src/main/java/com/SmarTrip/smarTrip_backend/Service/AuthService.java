package com.SmarTrip.smarTrip_backend.Service;

import com.SmarTrip.smarTrip_backend.Model.Role;
import com.SmarTrip.smarTrip_backend.Model.AuthenticationRequest;
import com.SmarTrip.smarTrip_backend.Model.User; // Change import to your custom User class
import com.SmarTrip.smarTrip_backend.Repositry.UserRepository;
import com.SmarTrip.smarTrip_backend.Security.AuthenticationResponse;
import com.SmarTrip.smarTrip_backend.Security.JwtService;
import com.SmarTrip.smarTrip_backend.Security.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // Add this import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // In the register method
    public AuthenticationResponse register(RegisterRequest request) {

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Create new user
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .photoUrl(request.getPhotoUrl()) // Add this line
                .role(Role.USER)
                .build();
    


        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);

        user.setToken(jwtToken);
        // Save user to database
        userRepository.save(user);
        // Return response with token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Find user by email
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);

        // Return response with token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public void updateProfilePicture(String userId, String photoUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPhotoUrl(photoUrl);
        userRepository.save(user);
    }

    // Add this method to your AuthService class

    public Map<String, Object> getUserProfileFromToken(String token) {
        // Extract email from token
        String userEmail = jwtService.extractUsername(token);
        
        if (userEmail != null) {
            // Find user by email
            User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Create response with user details
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("firstName", user.getFirstName());
            userDetails.put("lastName", user.getLastName());
            userDetails.put("email", user.getEmail());
            userDetails.put("photoUrl", user.getPhotoUrl());
            
            return userDetails;
        }
        
        throw new RuntimeException("Invalid token");
    }
}