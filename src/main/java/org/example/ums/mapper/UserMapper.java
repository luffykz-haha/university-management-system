package org.example.ums.mapper;

import org.example.ums.dto.user.UserPatchRequest;
import org.example.ums.dto.user.UserPostRequest;
import org.example.ums.dto.user.UserResponse;
import org.example.ums.model.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toDto(User user);

    List<UserResponse> toDto(List<User> users);

    User fromDto(UserPostRequest dto);

    User fromDto(UserPatchRequest dto);
}
