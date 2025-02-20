package org.example.user.repository.training.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.user.repository.training.model.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryImplTest {
    private static final String USER_TABLE_NAME = "users";
    private static final int USER_ID = 1;
    private static final int UNKNOWN_USER_ID = 999;

    private static final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15"))
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    private static UserRepositoryImpl repository;
    private static final User user = new User(USER_ID, "Костя");
    private static DataSource dataSource;

    @BeforeAll
    static void startContainer() {
        container.start();
        dataSource = createDataSource(container);
        createTable(dataSource);
        repository = new UserRepositoryImpl(dataSource);
    }

    @AfterAll
    static void stopContainer() {
        container.stop();
    }

    @Test
    void saveUser() {
        truncateTable(dataSource);
        boolean isUserSaved = repository.save(user);
        assertTrue(isUserSaved);

        Optional<User> loadedUser = getUser(dataSource, user.getId());

        assertTrue(loadedUser.isPresent());
        assertEquals(user.getId(), loadedUser.get().getId());
        assertEquals(user.getName(), loadedUser.get().getName());
    }

    @Test
    void loadUser() {
        truncateTable(dataSource);
        saveUser(dataSource, user);

        Optional<User> loadedUser = repository.load(1);

        assertEquals(Optional.of(user), loadedUser);
    }

    @Test
    void loadUnknownUser() {
        truncateTable(dataSource);
        saveUser(dataSource, user);

        Optional<User> loadedUser = repository.load(UNKNOWN_USER_ID);

        assertTrue(loadedUser.isEmpty());
    }

    @Test
    void loadFromEmptyTable() {
        truncateTable(dataSource);

        Optional<User> loadedUser = repository.load(USER_ID);

        assertTrue(loadedUser.isEmpty());
    }

    private static DataSource createDataSource(PostgreSQLContainer<?> container) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(container.getJdbcUrl());
        config.setUsername(container.getUsername());
        config.setPassword(container.getPassword());

        return new HikariDataSource(config);
    }

    private static void createTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME + " (id SERIAL PRIMARY KEY, name VARCHAR(255))");
        } catch (Exception e) {
            throw new RuntimeException("Error creating table", e);
        }
    }

    private static void truncateTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE " + USER_TABLE_NAME);
        } catch (Exception e) {
            throw new RuntimeException("Error truncating table", e);
        }
    }

    private static Optional<User> getUser(DataSource dataSource, int id) {

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM " + USER_TABLE_NAME + " WHERE id = ?")) {
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new User(resultSet.getInt("id"), resultSet.getString("name")));
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error truncating table", e);
        }
    }

    private static void saveUser(DataSource dataSource, User user) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("INSERT INTO " + USER_TABLE_NAME + " (id, name) VALUES (?, ?)")) {
            statement.setInt(1, user.getId());
            statement.setString(2, user.getName());
            if (statement.executeUpdate() < 1) {
                throw new RuntimeException("Error saving user");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error truncating table", e);
        }
    }
}
