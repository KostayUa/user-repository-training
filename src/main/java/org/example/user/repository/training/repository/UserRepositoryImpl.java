package org.example.user.repository.training.repository;

import org.example.user.repository.training.model.User;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class UserRepositoryImpl implements UserRepository {
    private final DataSource dataSource;

    public UserRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean save(User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            statement.setInt(1, user.getId());
            statement.setString(2, user.getName());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user", e);
        }
    }

    @Override
    public Optional<User> load(int id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_SQL)) {
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new User(resultSet.getInt("id"), resultSet.getString("name")));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user", e);
        }
    }

    private final static String SELECT_SQL = "SELECT id, name FROM users WHERE id = ?";
    private final static String INSERT_SQL = "INSERT INTO users (id, name) VALUES (?, ?) ON CONFLICT DO NOTHING";
}
