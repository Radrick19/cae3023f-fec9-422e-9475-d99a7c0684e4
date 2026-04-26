package org.example.codeconfirmapp.dao;

import org.example.codeconfirmapp.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserDao {
    void save(User user) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    Optional<User> findById(UUID id) throws SQLException;

    boolean existsAdmin() throws SQLException;

    List<User> findAllNonAdmins() throws SQLException;

    boolean deleteById(UUID id) throws SQLException;
}
