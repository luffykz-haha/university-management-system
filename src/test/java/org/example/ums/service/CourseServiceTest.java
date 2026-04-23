package org.example.ums.service;

import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Course;
import org.example.ums.repository.CourseRepository;
import org.example.ums.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CourseService courseService;

    private Course sampleCourse;
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String TEACHER_TOKEN = "teacher-token";
    private static final String STUDENT_TOKEN = "student-token";

    @BeforeEach
    void setUp() {
        sampleCourse = new Course();
        sampleCourse.setId(1);
        sampleCourse.setTitle("Introduction to Java");
        sampleCourse.setDescription("A beginner-friendly Java course.");
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: should return all courses from repository")
    void findAll_shouldReturnAllCourses() {
        when(courseRepository.findAll()).thenReturn(List.of(sampleCourse));

        List<Course> result = courseService.findAll();

        assertThat(result).hasSize(1).containsExactly(sampleCourse);
        verify(courseRepository).findAll();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById: existing id should return the course")
    void findById_existingCourse_shouldReturnCourse() {
        when(courseRepository.findById(1)).thenReturn(Optional.of(sampleCourse));

        Course result = courseService.findById(1);

        assertThat(result).isEqualTo(sampleCourse);
    }

    @Test
    @DisplayName("findById: missing id should throw NotFoundException")
    void findById_missingCourse_shouldThrowNotFoundException() {
        when(courseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.findById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: admin token should save and return the course")
    void create_adminToken_shouldSaveAndReturnCourse() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");

        Course newCourse = new Course();
        newCourse.setTitle("Data Structures");
        newCourse.setDescription("Algorithms and data structures.");

        Course result = courseService.create(newCourse, ADMIN_TOKEN);

        assertThat(result).isEqualTo(newCourse);
        verify(courseRepository).save(newCourse);
    }

    @Test
    @DisplayName("create: teacher token should save and return the course")
    void create_teacherToken_shouldSaveAndReturnCourse() {
        when(jwtUtil.extractSingleUserRole(TEACHER_TOKEN)).thenReturn("ROLE_TEACHER");

        Course newCourse = new Course();
        newCourse.setTitle("Advanced Java");
        newCourse.setDescription("Deep dive into Java.");

        Course result = courseService.create(newCourse, TEACHER_TOKEN);

        assertThat(result).isEqualTo(newCourse);
        verify(courseRepository).save(newCourse);
    }

    @Test
    @DisplayName("create: student token should throw ForbiddenAccessException")
    void create_studentToken_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        Course newCourse = new Course();
        newCourse.setTitle("Unauthorized Course");

        assertThatThrownBy(() -> courseService.create(newCourse, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Insufficient rights");

        verify(courseRepository, never()).save(any());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("update: should update non-blank fields and persist")
    void update_adminToken_shouldUpdateFieldsAndSave() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");
        when(courseRepository.findById(1)).thenReturn(Optional.of(sampleCourse));

        Course updatePayload = new Course();
        updatePayload.setTitle("Java Advanced");
        updatePayload.setDescription("Updated description.");

        Course result = courseService.update(1, updatePayload, ADMIN_TOKEN);

        assertThat(result.getTitle()).isEqualTo("Java Advanced");
        assertThat(result.getDescription()).isEqualTo("Updated description.");
        verify(courseRepository).save(updatePayload);
    }

    @Test
    @DisplayName("update: blank fields should fall back to existing course values")
    void update_blankFields_shouldKeepExistingValues() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");
        when(courseRepository.findById(1)).thenReturn(Optional.of(sampleCourse));

        Course updatePayload = new Course();
        updatePayload.setTitle("");
        updatePayload.setDescription("");

        Course result = courseService.update(1, updatePayload, ADMIN_TOKEN);

        assertThat(result.getTitle()).isEqualTo(sampleCourse.getTitle());
        assertThat(result.getDescription()).isEqualTo(sampleCourse.getDescription());
    }

    @Test
    @DisplayName("update: student token should throw ForbiddenAccessException")
    void update_studentToken_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        assertThatThrownBy(() -> courseService.update(1, new Course(), STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById: admin token should delete the course")
    void deleteById_adminToken_shouldDeleteCourse() {
        when(jwtUtil.extractSingleUserRole(ADMIN_TOKEN)).thenReturn("ROLE_ADMIN");

        courseService.deleteById(1, ADMIN_TOKEN);

        verify(courseRepository).deleteById(1);
    }

    @Test
    @DisplayName("deleteById: student token should throw ForbiddenAccessException")
    void deleteById_studentToken_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractSingleUserRole(STUDENT_TOKEN)).thenReturn("ROLE_STUDENT");

        assertThatThrownBy(() -> courseService.deleteById(1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class);

        verify(courseRepository, never()).deleteById(anyInt());
    }
}
