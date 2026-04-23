package org.example.ums.service;

import jakarta.validation.ValidationException;
import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Role;
import org.example.ums.model.User;
import org.example.ums.repository.UserRepository;
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
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User studentUser;
    private static final String ADMIN_TOKEN = "admin-token";
    private static final String STUDENT_TOKEN = "student-token";

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1);
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("adminpass");
        adminUser.setRole(Role.ADMIN);

        studentUser = new User();
        studentUser.setId(2);
        studentUser.setName("Student User");
        studentUser.setEmail("student@example.com");
        studentUser.setPassword("studentpass");
        studentUser.setRole(Role.STUDENT);
    }

    // ── findAll ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findAll: should return all users from repository")
    void findAll_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(adminUser, studentUser));

        List<User> result = userService.findAll();

        assertThat(result).hasSize(2).containsExactly(adminUser, studentUser);
        verify(userRepository).findAll();
    }

    // ── findById ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("findById: existing id should return the user")
    void findById_existingUser_shouldReturnUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        User result = userService.findById(1);

        assertThat(result).isEqualTo(adminUser);
    }

    @Test
    @DisplayName("findById: missing id should throw NotFoundException")
    void findById_missingUser_shouldThrowNotFoundException() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create: should assign STUDENT role and persist user")
    void create_shouldSetStudentRoleAndSave() {
        User newUser = new User();
        newUser.setName("New Student");
        newUser.setEmail("new@example.com");
        newUser.setPassword("pass");

        User result = userService.create(newUser);

        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        verify(userRepository).save(newUser);
    }

    // ── updateCredentials ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateCredentials: correct password, same user, should update name")
    void updateCredentials_sameUser_correctPassword_shouldUpdateName() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));

        User updatePayload = new User();
        updatePayload.setName("Updated Name");
        updatePayload.setEmail("student@example.com");
        updatePayload.setPassword("studentpass");
        updatePayload.setRole(Role.STUDENT);

        User result = userService.updateCredentials(updatePayload, 2, STUDENT_TOKEN);

        assertThat(result.getName()).isEqualTo("Updated Name");
        verify(userRepository).save(updatePayload);
    }

    @Test
    @DisplayName("updateCredentials: wrong password should throw ValidationException")
    void updateCredentials_wrongPassword_shouldThrowValidationException() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));

        User updatePayload = new User();
        updatePayload.setName("Updated Name");
        updatePayload.setPassword("wrongpass");

        assertThatThrownBy(() -> userService.updateCredentials(updatePayload, 2, STUDENT_TOKEN))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Incorrect password");
    }

    @Test
    @DisplayName("updateCredentials: non-admin updating another user should throw ForbiddenAccessException")
    void updateCredentials_differentUser_nonAdmin_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        User updatePayload = new User();
        updatePayload.setPassword("adminpass");

        assertThatThrownBy(() -> userService.updateCredentials(updatePayload, 1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class);
    }

    // ── updateRole ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateRole: admin requester should update user role")
    void updateRole_adminRequester_shouldUpdateRole() {
        when(jwtUtil.extractUserEmail(ADMIN_TOKEN)).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(studentUser));

        User result = userService.updateRole(Role.TEACHER, 2, ADMIN_TOKEN);

        assertThat(result.getRole()).isEqualTo(Role.TEACHER);
        verify(userRepository).save(studentUser);
    }

    @Test
    @DisplayName("updateRole: non-admin requester should throw ForbiddenAccessException")
    void updateRole_nonAdminRequester_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> userService.updateRole(Role.ADMIN, 1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class)
                .hasMessageContaining("Insufficient rights");
    }

    // ── deleteById ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteById: user deleting their own account should succeed")
    void deleteById_sameUser_shouldDelete() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));

        userService.deleteById(2, STUDENT_TOKEN);

        verify(userRepository).deleteById(2);
    }

    @Test
    @DisplayName("deleteById: non-admin deleting another user should throw ForbiddenAccessException")
    void deleteById_differentUser_nonAdmin_shouldThrowForbiddenAccessException() {
        when(jwtUtil.extractUserEmail(STUDENT_TOKEN)).thenReturn("student@example.com");
        when(userRepository.findByEmail("student@example.com")).thenReturn(Optional.of(studentUser));

        assertThatThrownBy(() -> userService.deleteById(1, STUDENT_TOKEN))
                .isInstanceOf(ForbiddenAccessException.class);

        verify(userRepository, never()).deleteById(anyInt());
    }

    @Test
    @DisplayName("deleteById: admin deleting another user should succeed")
    void deleteById_adminDeletingOtherUser_shouldDelete() {
        when(jwtUtil.extractUserEmail(ADMIN_TOKEN)).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));

        userService.deleteById(2, ADMIN_TOKEN);

        verify(userRepository).deleteById(2);
    }
}
