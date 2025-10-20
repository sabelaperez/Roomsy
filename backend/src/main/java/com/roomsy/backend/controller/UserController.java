package com.roomsy.backend.controller;

import com.roomsy.backend.exception.DuplicateResourceException;
import com.roomsy.backend.model.User;
import com.roomsy.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("users")
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ResponseEntity<?> createUser(@RequestBody @Valid User user) throws DuplicateResourceException {
        user = userService.createUser(user);
        return ResponseEntity.ok(user);
    }
}
