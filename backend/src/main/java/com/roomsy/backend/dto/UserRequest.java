package com.roomsy.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for creating an user")
public class UserRequest {
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

    public UserRequest() {}

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
