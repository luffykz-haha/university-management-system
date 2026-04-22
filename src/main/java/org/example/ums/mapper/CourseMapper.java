package org.example.ums.mapper;

import org.example.ums.dto.course.CourseRequest;
import org.example.ums.dto.course.CourseResponse;
import org.example.ums.model.Course;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    CourseResponse toDto(Course course);

    List<CourseResponse> toDto(List<Course> courses);

    Course fromDto(CourseRequest courseRequest);
}
