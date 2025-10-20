package com.roomsy.backend.service;

import com.roomsy.backend.exception.DuplicateResourceException;
import com.roomsy.backend.exception.ResourceNotFoundException;
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

    public User createUser(@NonNull User user) throws DuplicateResourceException {
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateResourceException("Email already in use");
        }

        return userRepository.save(user);
    }

    @Transactional
    public User updateEmail(UUID id, String newEmail) throws ResourceNotFoundException, DuplicateResourceException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (userRepository.existsByEmail(newEmail) && !user.getEmail().equals(newEmail)) {
            throw new DuplicateResourceException("Email already in use");
        }
        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    // Completar cuando se implemente seguridad
    @Transactional
    public void updatePassword(UUID id, String currentPassword, String newPassword) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        // verify currentPassword (compare hashes)...
        // hash newPassword...
        String hashedNewPassword = "password";
        user.setHashPassword(hashedNewPassword);
        userRepository.save(user);
    }

    // Crear método de patchUser con un PatchUserDTO con username y fullname
    /*    @Transactional
    public User patchUser(UUID id, PatchUserDto dto) throws Exception {
        User user = userRepository.findById(id)
                     .orElseThrow(() -> new Exception("User not found"));
        if (dto.getUsername() != null) {
            user.setUsername(dto.getUsername());
        }
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        return userRepository.save(user);
    }*/

    public void deactivateUser(@NonNull UUID id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public User activateUser(@NonNull UUID id) throws ResourceNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setActive(true);
        return userRepository.save(user);
    }

    public void deleteUser(@NonNull UUID id) throws ResourceNotFoundException {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean emailExists(@NonNull String email) {
        return userRepository.existsByEmail(email);
    }
}
