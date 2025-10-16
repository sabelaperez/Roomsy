package com.roomsy.backend.service;

import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.repository.GroupRepository;
import com.roomsy.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository, GroupRepository groupRepository) {
        this.userRepository = userRepository;
    }

    public User addUser(@NonNull User user) throws Exception {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("Email already in use");
        }

        return userRepository.save(user);
    }

    public User updateUser(@NonNull UUID id, @NonNull User updatedUser) throws Exception {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));

        // Update only non-null fields
        if (updatedUser.getEmail() != null) {
            // Check if email is already taken by another user
            if (userRepository.existsByEmail(updatedUser.getEmail()) &&
                    !existingUser.getEmail().equals(updatedUser.getEmail())) {
                throw new Exception("Email already in use");
            }
            existingUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }

        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }

        if (updatedUser.getHashPassword() != null) {
            existingUser.setHashPassword(updatedUser.getHashPassword());
        }

        return userRepository.save(existingUser);
    }

    public void deactivateUser(@NonNull UUID id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public User activateUser(@NonNull UUID id) throws Exception {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found with id: " + id));
        user.setActive(true);
        return userRepository.save(user);
    }

    public void deleteUser(@NonNull UUID id) throws Exception {
        if (!userRepository.existsById(id)) {
            throw new Exception("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean emailExists(@NonNull String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameExists(@NonNull String username) {
        return userRepository.existsByUsername(username);
    }
}
