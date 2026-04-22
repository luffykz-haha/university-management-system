package org.example.ums.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPostRequest {
    @NotBlank(message = "User name can not be empty")
    String name;

    @NotBlank(message = "Email can not be empty")
    @Email(message = "Incorrect email")
    String email;

    @NotBlank(message = "Password can not be empty")
    String password;
}
