package org.example.ums.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Course;
import org.example.ums.repository.CourseRepository;
import org.example.ums.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@Service
public class CourseService {
    CourseRepository courseRepository;

    JwtUtil jwtUtil;


    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    public Course findById(int courseId) {
        return findCourseByIdOrElseThrow(courseId);
    }

    public Course create(Course course, String token) {
        assertAdminOrTeacherRole(token);

        courseRepository.save(course);

        return course;
    }

    public Course update(int courseId, Course updateCourse, String token) {
        assertAdminOrTeacherRole(token);

        Course oldCourse = findCourseByIdOrElseThrow(courseId);

        if (updateCourse.getDescription() == null
                || updateCourse.getDescription().isBlank()) {
            updateCourse.setDescription(oldCourse.getDescription());
        }

        if (updateCourse.getTitle() == null
                || updateCourse.getTitle().isBlank()) {
            updateCourse.setTitle(oldCourse.getTitle());
        }


        courseRepository.save(updateCourse);
        return updateCourse;
    }

    public void deleteById(int courseId, String token) {
        assertAdminOrTeacherRole(token);

        courseRepository.deleteById(courseId);
    }

    private Course findCourseByIdOrElseThrow(int courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with '%d' id not found.", courseId)
                ));
    }

    private void assertAdminOrTeacherRole(String token) {
        String role = jwtUtil.extractSingleUserRole(token);

        if (!role.equals("ROLE_ADMIN") && !role.equals("ROLE_TEACHER")) {
            throw new ForbiddenAccessException("Insufficient rights to proceed.");
        }
    }
}
