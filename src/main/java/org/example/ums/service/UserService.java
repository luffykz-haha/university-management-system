package org.example.ums.service;

import jakarta.validation.ValidationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.example.ums.exception.ForbiddenAccessException;
import org.example.ums.exception.NotFoundException;
import org.example.ums.model.Role;
import org.example.ums.model.User;
import org.example.ums.repository.UserRepository;
import org.example.ums.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor

@Service
public class UserService {
    UserRepository userRepository;

    JwtUtil jwtUtil;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(int userId) {
        return checkIfUserExistsById(userId);
    }

    public User create(User user) {
        user.setRole(Role.STUDENT);

        userRepository.save(user);

        return user;
    }

    public User updateCredentials(User updateUser, int updateUserId, String token) {
        User requester = findUserByToken(token);

        // if requester and user that is being updated are equal,
        // no need to issue another call to DB
        User oldUser;
        if (updateUserId == requester.getId()) {
            oldUser = requester;
        } else {
            oldUser = checkIfUserExistsById(updateUserId);
        }

        // Check if password is valid
        if (!oldUser.getPassword().equals(updateUser.getPassword())) {
            throw new ValidationException("Incorrect password.");
        }

        if (updateUserId != requester.getId() && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("Insufficient rights to proceed.");
        }

        updateUser.setId(updateUserId);


        //Leave the old values, if new ones are not specified
        if (updateUser.getName() == null || updateUser.getName().isBlank()) {
            updateUser.setName(oldUser.getName());
        }

        if (updateUser.getEmail() == null || updateUser.getName().isBlank()) {
            updateUser.setEmail(oldUser.getEmail());
        }


        userRepository.save(updateUser);

        return updateUser;
    }

    public User updateRole(Role role, int updateUserId, String token) {
        User requester = findUserByToken(token);

        if (requester.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("Insufficient rights to proceed.");
        }


        User updateUser;
        if (updateUserId == requester.getId()) {
            updateUser = requester;
        } else {
            updateUser = checkIfUserExistsById(updateUserId);
        }

        updateUser.setRole(role);

        userRepository.save(updateUser);

        return updateUser;
    }

    public void deleteById(int deleteUserId, String token) {
        User requester = findUserByToken(token);

        if (deleteUserId != requester.getId() && requester.getRole() != Role.ADMIN) {
            throw new ForbiddenAccessException("Insufficient rights to proceed.");
        }

        userRepository.deleteById(deleteUserId);
    }

    private User checkIfUserExistsById(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with '%d' id not found.", userId)
                ));
    }

    private User findUserByToken(String token) {
        String email = jwtUtil.extractUserEmail(token);

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format("User with '%s' email not found", email)));
    }
}
