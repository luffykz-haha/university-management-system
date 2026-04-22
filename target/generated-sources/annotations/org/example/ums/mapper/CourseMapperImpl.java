package org.example.ums.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.ums.dto.course.CourseRequest;
import org.example.ums.dto.course.CourseResponse;
import org.example.ums.model.Course;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-22T22:47:04+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class CourseMapperImpl implements CourseMapper {

    @Override
    public CourseResponse toDto(Course course) {
        if ( course == null ) {
            return null;
        }

        CourseResponse courseResponse = new CourseResponse();

        courseResponse.setId( course.getId() );
        courseResponse.setTitle( course.getTitle() );
        courseResponse.setDescription( course.getDescription() );

        return courseResponse;
    }

    @Override
    public List<CourseResponse> toDto(List<Course> courses) {
        if ( courses == null ) {
            return null;
        }

        List<CourseResponse> list = new ArrayList<CourseResponse>( courses.size() );
        for ( Course course : courses ) {
            list.add( toDto( course ) );
        }

        return list;
    }

    @Override
    public Course fromDto(CourseRequest courseRequest) {
        if ( courseRequest == null ) {
            return null;
        }

        Course course = new Course();

        course.setTitle( courseRequest.getTitle() );
        course.setDescription( courseRequest.getDescription() );

        return course;
    }
}
