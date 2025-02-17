package org.example.user.repository.training.repository;

import org.example.user.repository.training.model.User;

import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final String url;
    private final String username;
    private final String password;

    public UserRepositoryImpl(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public void save(User user) {
        String query = "INSERT INTO users (id, name) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, user.getId());
            statement.setString(2, user.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> load(int id) {
        String query = "SELECT id, name FROM users WHERE id = ?";
        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new User(resultSet.getInt("id"), resultSet.getString("name")));
            } else {
                return Optional.empty(); //if User not found(can't be null)
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user", e);
        }
    }
}
