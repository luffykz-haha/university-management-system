package org.example.ums.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Course;
import org.example.ums.model.Enrollment;
import org.example.ums.model.User;
import org.example.ums.repository.CourseRepository;
import org.example.ums.repository.EnrollmentRepository;
import org.example.ums.repository.UserRepository;
import org.example.ums.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@Service
public class EnrollmentService {
    EnrollmentRepository enrollmentRepository;

    UserRepository userRepository;

    CourseRepository courseRepository;

    JwtUtil jwtUtil;

    public List<Enrollment> findAll(String token) {
        User requester = findUserByToken(token);

        if (assertStudentRole(token)) {
            return enrollmentRepository.findByStudent_Id(requester.getId());
        }

        if (assertAdminOrTeacherRole(token)) {
            return enrollmentRepository.findAll();
        }

        return List.of();
    }

    public Enrollment findById(int enrollmentId, String token) {
        String userEmail = jwtUtil.extractUserEmail(token);

        Enrollment enrollment = checkIfEnrollmentExistsById(enrollmentId);

        if (assertAdminOrTeacherRole(token)
                || enrollment.getStudent().getEmail().equals(userEmail)) {

            return enrollment;
        } else {
            throw new NotFoundException("Enrollment not found.");
        }
    }

    public Enrollment create(int studentId, int courseId, String token) {
        assertAdminOrTeacherRoleOrElseThrow(token);

        User student = checkIfUserExistsById(studentId);
        Course course = checkIfCourseExistsById(courseId);


        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setCreatedAt(LocalDateTime.now());

        enrollmentRepository.save(enrollment);

        return enrollment;
    }

    public Enrollment update(int enrollmentId, int studentId, int courseId, String token) {
        assertAdminOrTeacherRoleOrElseThrow(token);

        Enrollment enrollment = checkIfEnrollmentExistsById(enrollmentId);

        if (studentId != 0) {
            User student = checkIfUserExistsById(studentId);
            enrollment.setStudent(student);
        }

        if (courseId == 0) {
            Course course = checkIfCourseExistsById(courseId);
            enrollment.setCourse(course);
        }


        enrollmentRepository.save(enrollment);

        return enrollment;
    }

    public void deleteById(int enrollmentId, String token) {
        assertAdminOrTeacherRoleOrElseThrow(token);

        enrollmentRepository.deleteById(enrollmentId);
    }

    private Enrollment checkIfEnrollmentExistsById(int enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Enrollment with '%d' id not found.", enrollmentId)
                ));
    }

    private User checkIfUserExistsById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with '%d' id not found.", userId)
                ));
    }

    private Course checkIfCourseExistsById(int courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Course with '%d' id not found.", courseId)
                ));
    }

    private boolean assertAdminOrTeacherRole(String token) {
        String role = jwtUtil.extractSingleUserRole(token);

        return role.equals("ROLE_ADMIN") || role.equals("ROLE_TEACHER");
    }

    private boolean assertStudentRole(String token) {
        String role = jwtUtil.extractSingleUserRole(token);

        return role.equals("ROLE_STUDENT");
    }

    private User findUserByToken(String token) {
        String email = jwtUtil.extractUserEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with '%s' email not found", email)));
    }

    private void assertAdminOrTeacherRoleOrElseThrow(String token) {
        if (!assertAdminOrTeacherRole(token)) {
            throw new ForbiddenAccessException("Insufficient rights to proceed.");
        }
    }
}
