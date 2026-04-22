package org.example.ums.mapper;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.example.ums.dto.course.CourseResponse;
import org.example.ums.dto.enrollment.EnrollmentResponse;
import org.example.ums.dto.user.UserResponse;
import org.example.ums.model.Course;
import org.example.ums.model.Enrollment;
import org.example.ums.model.User;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-22T22:47:04+0800",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class EnrollmentMapperImpl implements EnrollmentMapper {

    @Override
    public EnrollmentResponse toDto(Enrollment enrollment) {
        if ( enrollment == null ) {
            return null;
        }

        EnrollmentResponse enrollmentResponse = new EnrollmentResponse();

        enrollmentResponse.setId( enrollment.getId() );
        enrollmentResponse.setStudent( userToUserResponse( enrollment.getStudent() ) );
        enrollmentResponse.setCourse( courseToCourseResponse( enrollment.getCourse() ) );
        enrollmentResponse.setCreatedAt( enrollment.getCreatedAt() );

        return enrollmentResponse;
    }

    @Override
    public List<EnrollmentResponse> toDto(List<Enrollment> enrollments) {
        if ( enrollments == null ) {
            return null;
        }

        List<EnrollmentResponse> list = new ArrayList<EnrollmentResponse>( enrollments.size() );
        for ( Enrollment enrollment : enrollments ) {
            list.add( toDto( enrollment ) );
        }

        return list;
    }

    protected UserResponse userToUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        UserResponse userResponse = new UserResponse();

        userResponse.setId( user.getId() );
        userResponse.setName( user.getName() );
        userResponse.setEmail( user.getEmail() );

        return userResponse;
    }

    protected CourseResponse courseToCourseResponse(Course course) {
        if ( course == null ) {
            return null;
        }

        CourseResponse courseResponse = new CourseResponse();

        courseResponse.setId( course.getId() );
        courseResponse.setTitle( course.getTitle() );
        courseResponse.setDescription( course.getDescription() );

        return courseResponse;
    }
}
