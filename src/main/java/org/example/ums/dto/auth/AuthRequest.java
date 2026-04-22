package org.example.ums.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthRequest {
    @NotBlank(message = "Email can not be empty")
    @Email(message = "Incorrect email")
    private String email;

    @NotBlank(message = "Password can not be empty")
    private String password;
}
