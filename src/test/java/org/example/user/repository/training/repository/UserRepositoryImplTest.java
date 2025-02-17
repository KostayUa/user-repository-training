package org.example.user.repository.training.repository;

import org.example.user.repository.training.model.User;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserRepositoryImplTest {
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:15"))
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");

    private UserRepositoryImpl repository;
    private static final User user = new User(1, "Костя");

    @BeforeAll
    static void startContainer() {
        postgres.start();
    }

    @BeforeEach
    void setUp() throws Exception {
        String url = postgres.getJdbcUrl();
        String user = postgres.getUsername();
        String password = postgres.getPassword();

        repository = new UserRepositoryImpl(url, user, password);

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users (id SERIAL PRIMARY KEY, name VARCHAR(255))");
        }
    }

    @Test
    @Order(1)
    void saveUser() {
        repository.save(user);

        Optional<User> loadedUser = repository.load(1);
        assertTrue(loadedUser.isPresent());
        assertEquals(user.getId(), loadedUser.get().getId());
        assertEquals(user.getName(), loadedUser.get().getName());
    }

    @Test
    @Order(2)
    void loadUser() {
        Optional<User> loadedUser = repository.load(1);
        assertEquals(Optional.of(user), loadedUser);
    }

    @Test
    @Order(3)
    void loadNonExistingUser() {
        assertTrue(repository.load(999).isEmpty());
    }

    @AfterAll
    static void stopContainer() {
        postgres.stop();
    }
}
