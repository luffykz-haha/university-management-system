package org.example.ums.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.ums.dto.enrollment.EnrollmentResponse;
import org.example.ums.mapper.EnrollmentMapper;
import org.example.ums.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.example.ums.util.RequestConstants.AUTH_USER_JWT;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {
    EnrollmentService enrollmentService;

    EnrollmentMapper enrollmentMapper;

    @GetMapping
    public List<EnrollmentResponse> findAll(
            @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        return enrollmentMapper.toDto(
                enrollmentService.findAll(token));
    }

    @GetMapping("/{id}")
    public EnrollmentResponse findById(
            @PathVariable(name = "id") int enrollmentId,
            @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        return enrollmentMapper.toDto(
                enrollmentService.findById(enrollmentId, token));
    }

    @PostMapping
    public EnrollmentResponse create(
            @RequestParam(name = "student") int studentId,
            @RequestParam(name = "course") int courseId,
            @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        return enrollmentMapper.toDto(
                enrollmentService.create(studentId, courseId, token));
    }

    @PatchMapping("/{id}")
    public EnrollmentResponse update(
            @PathVariable(name = "id") int enrollmentId,
            @RequestParam(name = "student", defaultValue = "0") int studentId,
            @RequestParam(name = "course", defaultValue = "0") int courseId,
            @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        return enrollmentMapper.toDto(
                enrollmentService.update(enrollmentId, studentId, courseId, token));
    }

    @DeleteMapping("/{id}")
    public void deleteById(
            @PathVariable(name = "id") int enrollmentId,
            @RequestHeader(AUTH_USER_JWT) String authHeader) {

        String token = extractToken(authHeader);

        enrollmentService.deleteById(enrollmentId, token);
    }

    // Util method, we clean here the "Bearer " part
    private String extractToken(String authHeader) {
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }
}
