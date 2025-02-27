package org.example.user.repository.training.repository;

import org.example.user.repository.training.model.Email;
import org.example.user.repository.training.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            statement.setString(3, user.getEmail().getValue());
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
                return Optional.of(
                    new User(
                        resultSet.getInt("id"),
                        resultSet.getString("name"),
                        Email.create(resultSet.getString("email"))
                    )
                );
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user", e);
        }
    }

    private final static String SELECT_SQL = "SELECT * FROM users WHERE id = ?";
    private final static String INSERT_SQL = "INSERT INTO users (id, name, email) VALUES (?, ?, ?) ON CONFLICT DO NOTHING";
}
