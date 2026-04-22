package org.example.ums.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.ums.dto.user.UserPatchRequest;
import org.example.ums.dto.user.UserResponse;
import org.example.ums.mapper.UserMapper;
import org.example.ums.model.Role;
import org.example.ums.model.User;
import org.example.ums.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.ums.util.RequestConstants.AUTH_USER_JWT;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@RestController
@RequestMapping("/users")
public class UserController {
    UserService userService;

    UserMapper userMapper;

    @GetMapping
    public List<UserResponse> findAll() {
        return userMapper.toDto(
                userService.findAll());
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable(name = "id") int userId) {
        return userMapper.toDto(
                userService.findById(userId));
    }

    @PatchMapping("/{id}")
    public UserResponse updateCredentials(@Valid @RequestBody UserPatchRequest userPatchRequest,
                               @PathVariable(name = "id") int updateUserId,
                               @RequestHeader(AUTH_USER_JWT) String authHeader) {
        String token = extractToken(authHeader);
        User user = userMapper.fromDto(userPatchRequest);

        return userMapper.toDto(
                userService.updateCredentials(user, updateUserId, token));
    }

    @PatchMapping("/role/{id}")
    public UserResponse updateRole(@RequestParam Role role,
                                   @PathVariable(name = "id") int updateUserId,
                                   @RequestHeader(AUTH_USER_JWT) String authHeader) {
        String token = extractToken(authHeader);

        return userMapper.toDto(
                userService.updateRole(role, updateUserId, token));
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable(name = "id") int deleteUserId,
                           @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        userService.deleteById(deleteUserId, token);
    }

    // Util method, we clean here the "Bearer " part
    private String extractToken(String authHeader) {
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
