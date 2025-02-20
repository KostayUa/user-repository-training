package org.example.user.repository.training.repository;

import org.example.user.repository.training.model.User;

import java.util.Optional;

public interface UserRepository {
    boolean save(User user);

    Optional<User> load(int id);
}
