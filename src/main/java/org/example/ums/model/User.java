package org.example.ums.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String name;

    String email;

    String password;

    @Enumerated(value = EnumType.STRING)
    Role role;

    @OneToMany(mappedBy = "student")
    List<Enrollment> enrollments;
}
