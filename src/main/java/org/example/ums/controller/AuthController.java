package org.example.ums.controller;

import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.example.ums.dto.auth.AuthRequest;
import org.example.ums.dto.auth.AuthResponse;
import org.example.ums.dto.user.UserPostRequest;
import org.example.ums.mapper.UserMapper;
import org.example.ums.model.User;
import org.example.ums.util.JwtUtil;
import org.example.ums.security.CustomUserDetails;
import org.example.ums.service.UserService;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@RestController
@RequestMapping("/auth")
public class AuthController {
    AuthenticationManager authManager;

    UserService userService;

    JwtUtil jwtUtil;

    PasswordEncoder passwordEncoder;

    UserMapper userMapper;


    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserPostRequest userRequest) {
        userRequest.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        User user = userMapper.fromDto(userRequest);

        userService.create(user);

        return ResponseEntity.ok("Registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(token));
    }
}
