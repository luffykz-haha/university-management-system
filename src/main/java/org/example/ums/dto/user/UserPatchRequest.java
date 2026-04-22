package org.example.ums.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPatchRequest {
    String name;

    @Email
    String email;

    // The idea is that the user would have to enter password
    // each time, they want to update their credentials
    @NotBlank(message = "Password can not be empty")
    String password;
}
