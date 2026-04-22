package org.example.ums.dto.enrollment;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.example.ums.dto.course.CourseResponse;
import org.example.ums.dto.user.UserResponse;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnrollmentResponse {
    int id;

    UserResponse student;

    CourseResponse course;

    LocalDateTime createdAt;
}
