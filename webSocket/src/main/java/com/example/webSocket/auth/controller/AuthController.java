package com.example.webSocket.auth.controller;

import com.example.webSocket.auth.entity.Admin;
import com.example.webSocket.auth.entity.User;
import com.example.webSocket.auth.repo.AdminRepository;
import com.example.webSocket.auth.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public AuthController(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @PostMapping("/register/user")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setUsername(request.get("username"));
        user.setPassword(request.get("password"));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody Map<String, String> request) {
        Admin admin = new Admin();
        admin.setUsername(request.get("username"));
        admin.setPassword(request.get("password"));
        adminRepository.save(admin);
        return ResponseEntity.ok("Admin registered successfully");
    }
}
