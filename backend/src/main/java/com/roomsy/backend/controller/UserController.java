package com.roomsy.backend.controller;

import com.roomsy.backend.dto.UserSummaryResponse;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "Endpoints for managing users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    @PostMapping
    public ResponseEntity<UserSummaryResponse> createUser(
        @Valid @RequestBody CreateUserRequest request
    ) {
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

    @Operation(summary = "Get all users", description = "Retrieves a list of all existing users in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users retrieved successfully"),
    })
    @GetMapping
    public ResponseEntity<List<UserSummaryResponse>> getUsers() {
        List<User> users = userService.getUsers();
        List<UserSummaryResponse> response = users.stream()
                .map(UserSummaryResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    public static class CreateUserRequest {

        @NotBlank
        @Email
        @Schema(description = "The email address of the user",
                example = "user@example.com")
        private String email;

        @NotBlank
        @Size(min = 4, max = 20)
        @Schema(description = "The username of the user",
                example = "john_doe",
                minLength = 4,
                maxLength = 20)
        private String username;

        @Size(min = 4, max = 50)
        @Schema(description = "The full name of the user",
                example = "John Doe",
                minLength = 4,
                maxLength = 50)
        private String fullName;

        /**
         * For now this is the value that will be stored in the user's hashPassword field.
         * In the future, replace this with a plain password and hash it before persisting.
         */
        @NotBlank
        @Schema(description = "The password of the user",
                example = "password_value")
        private String hashPassword;

        public CreateUserRequest() {}

        // getters & setters
        public String getEmail() { 
            return email; 
        }
        public void setEmail(String email) { 
            this.email = email; 
        }

        public String getUsername() { 
            return username; 
        }
        public void setUsername(String username) { 
            this.username = username; 
        }

        public String getFullName() { 
            return fullName; 
        }
        public void setFullName(String fullName) { 
            this.fullName = fullName; 
        }

        public String getHashPassword() { 
            return hashPassword; 
        }
        public void setHashPassword(String hashPassword) { 
            this.hashPassword = hashPassword; 
        }
    }
}

