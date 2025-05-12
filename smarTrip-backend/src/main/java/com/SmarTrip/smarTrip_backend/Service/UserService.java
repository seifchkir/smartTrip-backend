package com.SmarTrip.smarTrip_backend.Service;

import com.SmarTrip.smarTrip_backend.Model.User;
import com.SmarTrip.smarTrip_backend.Repositry.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // Add this method to get a user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public void updateProfilePicture(String userId, String imageUrl) {
        User user = getUserById(userId);
        user.setPhotoUrl(imageUrl);
        userRepository.save(user);
    }

    public void updateProfilePictureByEmail(String email, String imageUrl) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        user.setPhotoUrl(imageUrl);
        userRepository.save(user);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}