package org.example.codeconfirmapp.jdbc;

import org.example.codeconfirmapp.dao.UserDao;
import org.example.codeconfirmapp.model.Role;
import org.example.codeconfirmapp.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JdbcUserDao implements UserDao {
    private static final String USER_COLUMNS = "id, username, password_hash, role, email, phone, telegram_chat_id";
    private static final String SELECT_BY_USERNAME = "SELECT " + USER_COLUMNS + " FROM users WHERE username = ?";
    private static final String SELECT_BY_ID = "SELECT " + USER_COLUMNS + " FROM users WHERE id = ?";
    private static final String SELECT_NON_ADMINS = "SELECT " + USER_COLUMNS + " FROM users WHERE role <> 'ADMIN' ORDER BY username";

    private final DataSource dataSource;

    public JdbcUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(User user) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     INSERT INTO users (id, username, password_hash, role, email, phone, telegram_chat_id)
                     VALUES (?, ?, ?, ?, ?, ?, ?)
                     """)) {
            statement.setObject(1, user.id());
            statement.setString(2, user.username());
            statement.setString(3, user.passwordHash());
            statement.setString(4, user.role().name());
            statement.setString(5, user.email());
            statement.setString(6, user.phone());
            statement.setString(7, user.telegramChatId());
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_USERNAME)) {
            statement.setString(1, username);
            return findOne(statement);
        }
    }

    @Override
    public Optional<User> findById(UUID id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setObject(1, id);
            return findOne(statement);
        }
    }

    @Override
    public boolean existsAdmin() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN')
                     """);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBoolean(1);
        }
    }

    @Override
    public List<User> findAllNonAdmins() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_NON_ADMINS);
             ResultSet resultSet = statement.executeQuery()) {
            var users = new ArrayList<User>();
            while (resultSet.next()) {
                users.add(map(resultSet));
            }
            return users;
        }
    }

    @Override
    public boolean deleteById(UUID id) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setObject(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private User map(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                Role.valueOf(resultSet.getString("role")),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("telegram_chat_id")
        );
    }

    private Optional<User> findOne(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
        }
    }
}
