package org.example.ums.dto.course;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseRequest {
    @NotBlank(message = "Course title can not be empty!")
    String title;

    @NotBlank(message = "Course description can not be empty!")
    String description;
}
