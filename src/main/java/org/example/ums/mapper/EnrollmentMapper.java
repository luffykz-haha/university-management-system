package org.example.ums.mapper;

import org.example.ums.dto.enrollment.EnrollmentResponse;
import org.example.ums.model.Enrollment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper {
    EnrollmentResponse toDto(Enrollment enrollment);

    List<EnrollmentResponse> toDto(List<Enrollment> enrollments);
}
