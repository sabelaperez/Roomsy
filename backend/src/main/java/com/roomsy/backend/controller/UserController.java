package com.roomsy.backend.controller;

import com.roomsy.backend.dto.GroupResponse;
import com.roomsy.backend.dto.UserSummaryResponse;
import com.roomsy.backend.exception.DuplicateResourceException;
import com.roomsy.backend.model.Group;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserSummaryResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = new User(
                request.getEmail(),
                request.getUsername(),
                request.getFullName(),
                request.getHashPassword()
        );

        User saved = userService.createUser(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(UserSummaryResponse.fromEntity(saved));
    }


    public static class CreateUserRequest {

        @NotBlank
        @Email
        private String email;

        @NotBlank
        @Size(min = 4, max = 20)
        private String username;

        @Size(min = 4, max = 50)
        private String fullName;

        /**
         * For now this is the value that will be stored in the user's hashPassword field.
         * In the future, replace this with a plain password and hash it before persisting.
         */
        @NotBlank
        private String hashPassword;

        public CreateUserRequest() {}

        // getters & setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getHashPassword() { return hashPassword; }
        public void setHashPassword(String hashPassword) { this.hashPassword = hashPassword; }
    }

    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getUsers() {
        List<User> users = userService.getUsers();
        List<UserSummaryResponse> response = users.stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}

