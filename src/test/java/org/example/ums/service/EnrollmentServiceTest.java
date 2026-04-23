package org.example.ums.service;

import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Course;
import org.example.ums.model.Enrollment;
import org.example.ums.model.Role;
import org.example.ums.model.User;
import org.example.ums.repository.CourseRepository;
import org.example.ums.repository.EnrollmentRepository;
import org.example.ums.repository.UserRepository;
import org.example.ums.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User adminUser;
    private User studentUser;
    private Course sampleCourse;
    private Enrollment sampleEnrollment;

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String TEACHER_TOKEN = "teacher-token";
    private static final String STUDENT_TOKEN = "student-token";

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(Role.ADMIN);

        studentUser = new User();
        studentUser.setId(2);
        studentUser.setEmail("student@example.com");
        studentUser.setRole(Role.STUDENT);

        sampleCourse = new Course();
        sampleCourse.setId(1);
        sampleCourse.setTitle("Spring Boot Fundamentals");

        sampleEnrollment = new Enrollment();
        sampleEnrollment.setId(1);
        sampleEnrollment.setStudent(studentUser);
        sampleEnrollment.setCourse(sampleCourse);
        sampleEnrollment.setCreatedAt(LocalDateTime.now());
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: admin role should return all enrollments")
    void findAll_adminRole_shouldReturnAllEnrollments() {
        when(jwtUtil.extractUserEmail(ADMIN_TOKEN)).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");
        when(enrollmentRepository.findAll()).thenReturn(List.of(sampleEnrollment));

        List<Enrollment> result = enrollmentService.findAll(ADMIN_TOKEN);

        assertThat(result).hasSize(1).containsExactly(sampleEnrollment);
        verify(enrollmentRepository).findAll();
    }

    @Test
    @DisplayName("findAll: student role should return only their enrollments")
    void findAll_studentRole_shouldReturnOwnEnrollments() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");
        when(enrollmentRepository.findByStudent_Id(2)).thenReturn(List.of(sampleEnrollment));

        List<Enrollment> result = enrollmentService.findAll(STUDENT_TOKEN);

        assertThat(result).hasSize(1).containsExactly(sampleEnrollment);
        verify(enrollmentRepository).findByStudent_Id(2);
        verify(enrollmentRepository, never()).findAll();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById: admin role should return any enrollment")
    void findById_adminRole_shouldReturnEnrollment() {
        when(jwtUtil.extractUserEmail(ADMIN_TOKEN)).thenReturn("admin@example.com");
        when(enrollmentRepository.findById(1)).thenReturn(Optional.of(sampleEnrollment));
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");

        Enrollment result = enrollmentService.findById(1, ADMIN_TOKEN);

        assertThat(result).isEqualTo(sampleEnrollment);
    }

    @Test
    @DisplayName("findById: student who owns the enrollment should be able to view it")
    void findById_studentOwner_shouldReturnEnrollment() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(enrollmentRepository.findById(1)).thenReturn(Optional.of(sampleEnrollment));
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        Enrollment result = enrollmentService.findById(1, STUDENT_TOKEN);

        assertThat(result).isEqualTo(sampleEnrollment);
    }

    @Test
    @DisplayName("findById: student accessing another student's enrollment should throw NotFoundException")
    void findById_differentStudent_shouldThrowNotFoundException() {
        User otherStudent = new User();
        otherStudent.setId(3);
        otherStudent.setEmail("other@example.com");
        sampleEnrollment.setStudent(otherStudent);

        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(enrollmentRepository.findById(1)).thenReturn(Optional.of(sampleEnrollment));
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        assertThatThrownBy(() -> enrollmentService.findById(1, STUDENT_TOKEN))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Enrollment not found");
    }

    @Test
    @DisplayName("findById: missing enrollment id should throw NotFoundException")
    void findById_missingEnrollment_shouldThrowNotFoundException() {
        when(jwtUtil.extractUserEmail(ADMIN_TOKEN)).thenReturn("admin@example.com");
        when(enrollmentRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.findById(99, ADMIN_TOKEN))
                .isInstanceOf(NotFoundException.class);
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: admin role should create and return a new enrollment")
    void create_adminRole_shouldCreateEnrollment() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");
        when(userRepository.findById(2)).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(1)).thenReturn(Optional.of(sampleCourse));

        Enrollment result = enrollmentService.create(2, 1, ADMIN_TOKEN);

        assertThat(result.getStudent()).isEqualTo(studentUser);
        assertThat(result.getCourse()).isEqualTo(sampleCourse);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(enrollmentRepository).save(result);
    }

    @Test
    @DisplayName("create: teacher role should create and return a new enrollment")
    void create_teacherRole_shouldCreateEnrollment() {
        when(jwtUtil.extractSingleUserRole(TEACHER_TOKEN)).thenReturn("ROLE_TEACHER");
        when(userRepository.findById(2)).thenReturn(Optional.of(studentUser));
        when(courseRepository.findById(1)).thenReturn(Optional.of(sampleCourse));

        Enrollment result = enrollmentService.create(2, 1, TEACHER_TOKEN);

        assertThat(result.getStudent()).isEqualTo(studentUser);
        verify(enrollmentRepository).save(result);
    }

    @Test
    @DisplayName("create: student role should throw ForbiddenAccessException")
    void create_studentRole_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        assertThatThrownBy(() -> enrollmentService.create(2, 1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Insufficient rights");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("create: non-existent student id should throw NotFoundException")
    void create_nonExistentStudent_shouldThrowNotFoundException() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.create(99, 1, ADMIN_TOKEN))
                .isInstanceOf(NotFoundException.class);
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById: admin role should delete enrollment")
    void deleteById_adminRole_shouldDeleteEnrollment() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");

        enrollmentService.deleteById(1, ADMIN_TOKEN);

        verify(enrollmentRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteById: student role should throw ForbiddenAccessException")
    void deleteById_studentRole_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        assertThatThrownBy(() -> enrollmentService.deleteById(1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class);

        verify(enrollmentRepository, never()).deleteById(anyInt());
    }
}
