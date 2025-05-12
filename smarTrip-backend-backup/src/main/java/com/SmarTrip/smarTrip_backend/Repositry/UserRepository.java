package com.SmarTrip.smarTrip_backend.Repositry;

import com.SmarTrip.smarTrip_backend.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    
    // Add this method to check if a user with the given email exists
    boolean existsByEmail(String email);
}