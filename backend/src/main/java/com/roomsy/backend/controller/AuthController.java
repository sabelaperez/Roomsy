package com.roomsy.backend.controller;

import com.roomsy.backend.dto.AuthResponse;
import com.roomsy.backend.dto.LoginRequest;
import com.roomsy.backend.dto.RegisterRequest;
import com.roomsy.backend.model.User;
import com.roomsy.backend.security.CookieUtil;
import com.roomsy.backend.security.CustomUserDetails;
import com.roomsy.backend.security.JwtUtil;
import com.roomsy.backend.security.TokenService;
import com.roomsy.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;

    @Autowired
    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil,
            TokenService tokenService,
            CookieUtil cookieUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.cookieUtil = cookieUtil;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        // Hash the password
        String hashedPassword = passwordEncoder.encode(request.password());

        // Create user
        User user = new User(
                request.email(),
                request.username(),
                request.fullName(),
                hashedPassword
        );

        User savedUser = userService.createUser(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getId());

        // Store tokens in Redis
        tokenService.storeAccessToken(savedUser.getId(), accessToken);
        tokenService.storeRefreshToken(savedUser.getId(), refreshToken);

        // Set cookies
        response.addCookie(cookieUtil.createAccessTokenCookie(accessToken, jwtUtil.getAccessTokenExpiration()));
        response.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken, jwtUtil.getRefreshTokenExpiration()));

        AuthResponse authResponse = new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                savedUser.getFullName(),
                savedUser.getRole(),
                null,
                "Registration successful"
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authResponse);
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns JWT tokens in cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(userDetails.getId(), userDetails.getEmail(), userDetails.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getId());

        // Store tokens in Redis
        tokenService.storeAccessToken(userDetails.getId(), accessToken);
        tokenService.storeRefreshToken(userDetails.getId(), refreshToken);

        // Set cookies
        response.addCookie(cookieUtil.createAccessTokenCookie(accessToken, jwtUtil.getAccessTokenExpiration()));
        response.addCookie(cookieUtil.createRefreshTokenCookie(refreshToken, jwtUtil.getRefreshTokenExpiration()));

        // Get full user data
        User user = userService.getUserById(userDetails.getId());

        UUID groupId = user.getGroup() != null ? user.getGroup().getId() : null;

        AuthResponse authResponse = new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                groupId,
                "Login successful"
        );

        return ResponseEntity.ok(authResponse);
    }

    @Operation(summary = "Logout", description = "Revokes the current user's tokens and clears cookies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) {

        if (userDetails != null) {
            // Revoke all tokens for this user
            tokenService.revokeAllUserTokens(userDetails.getId());
        }

        // Clear cookies
        response.addCookie(cookieUtil.createExpiredAccessTokenCookie());
        response.addCookie(cookieUtil.createExpiredRefreshTokenCookie());

        return ResponseEntity.ok("Logout successful");
    }

    @Operation(summary = "Refresh token", description = "Generates a new access token using the refresh token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            HttpServletResponse response,
            @CookieValue(name = "${cookie.refresh-token-name}", required = false) String refreshToken) {

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        // Check if refresh token is valid in Redis
        if (!tokenService.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token revoked or expired");
        }

        // Extract user ID
        UUID userId = jwtUtil.extractUserId(refreshToken);
        User user = userService.getUserById(userId);

        // Generate new access token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), user.getRole());

        // Store new access token in Redis
        tokenService.storeAccessToken(user.getId(), newAccessToken);

        // Set new access token cookie
        response.addCookie(cookieUtil.createAccessTokenCookie(newAccessToken, jwtUtil.getAccessTokenExpiration()));

        return ResponseEntity.ok("Token refreshed successfully");
    }

    @Operation(summary = "Get current user", description = "Returns information about the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {

        if(userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.getUserById(userDetails.getId());
      
        UUID groupId = user.getGroup() != null ? user.getGroup().getId() : null;


        AuthResponse response = new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                groupId,
                "User retrieved successfully"
        );

        return ResponseEntity.ok(response);
    }
}