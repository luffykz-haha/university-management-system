package org.example.ums.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails adminUserDetails;
    private UserDetails studentUserDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

        adminUserDetails = new User(
                "admin@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        studentUserDetails = new User(
                "student@example.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_STUDENT"))
        );
    }

    // ── generateToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("generateToken: should produce a non-null, non-empty JWT string")
    void generateToken_shouldReturnNonEmptyString() {
        String token = jwtUtil.generateToken(adminUserDetails);

        assertThat(token).isNotNull().isNotBlank();
    }

    @Test
    @DisplayName("generateToken: tokens for different users should be distinct")
    void generateToken_differentUsers_shouldProduceDifferentTokens() {
        String adminToken = jwtUtil.generateToken(adminUserDetails);
        String studentToken = jwtUtil.generateToken(studentUserDetails);

        assertThat(adminToken).isNotEqualTo(studentToken);
    }

    // ── extractUserEmail ──────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUserEmail: should return the email embedded in the token")
    void extractUserEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateToken(adminUserDetails);

        String email = jwtUtil.extractUserEmail(token);

        assertThat(email).isEqualTo("admin@example.com");
    }

    @Test
    @DisplayName("extractUserEmail: student token should return student email")
    void extractUserEmail_studentToken_shouldReturnStudentEmail() {
        String token = jwtUtil.generateToken(studentUserDetails);

        String email = jwtUtil.extractUserEmail(token);

        assertThat(email).isEqualTo("student@example.com");
    }

    // ── extractUserRoles ──────────────────────────────────────────────────────

    @Test
    @DisplayName("extractUserRoles: should return the list of roles embedded in the token")
    void extractUserRoles_shouldReturnCorrectRoles() {
        String token = jwtUtil.generateToken(adminUserDetails);

        List<String> roles = jwtUtil.extractUserRoles(token);

        assertThat(roles).isNotEmpty().contains("ROLE_ADMIN");
    }

    @Test
    @DisplayName("extractUserRoles: student token should return ROLE_STUDENT")
    void extractUserRoles_studentToken_shouldReturnStudentRole() {
        String token = jwtUtil.generateToken(studentUserDetails);

        List<String> roles = jwtUtil.extractUserRoles(token);

        assertThat(roles).containsExactly("ROLE_STUDENT");
    }

    // ── extractSingleUserRole ─────────────────────────────────────────────────

    @Test
    @DisplayName("extractSingleUserRole: should return the first role from token")
    void extractSingleUserRole_shouldReturnFirstRole() {
        String token = jwtUtil.generateToken(adminUserDetails);

        String role = jwtUtil.extractSingleUserRole(token);

        assertThat(role).isEqualTo("ROLE_ADMIN");
    }

    // ── validateToken ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("validateToken: valid token for matching user should return true")
    void validateToken_validToken_shouldReturnTrue() {
        String token = jwtUtil.generateToken(adminUserDetails);

        boolean isValid = jwtUtil.validateToken(token, adminUserDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken: token issued for a different user should return false")
    void validateToken_mismatchedUser_shouldReturnFalse() {
        String adminToken = jwtUtil.generateToken(adminUserDetails);

        boolean isValid = jwtUtil.validateToken(adminToken, studentUserDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken: malformed token should throw an exception")
    void validateToken_malformedToken_shouldThrowException() {
        assertThatThrownBy(() -> jwtUtil.validateToken("not.a.valid.jwt", adminUserDetails))
                .isInstanceOf(Exception.class);
    }
}
