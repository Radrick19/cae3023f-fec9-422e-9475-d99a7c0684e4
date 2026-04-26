package org.example.codeconfirmapp.jdbc;

import org.example.codeconfirmapp.dao.OtpConfigDao;
import org.example.codeconfirmapp.model.OtpConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcOtpConfigDao implements OtpConfigDao {
    private static final String SELECT_CONFIG = "SELECT code_length, ttl_seconds FROM otp_config WHERE id = 1";
    private static final String UPDATE_CONFIG = """
            UPDATE otp_config
            SET code_length = ?, ttl_seconds = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = 1
            """;

    private final DataSource dataSource;

    public JdbcOtpConfigDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public OtpConfig getConfig() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_CONFIG);
             ResultSet resultSet = statement.executeQuery()) {
            if (!resultSet.next()) {
                throw new IllegalStateException("Запись с конфигурацией OTP отсутствует");
            }
            return new OtpConfig(resultSet.getInt("code_length"), resultSet.getInt("ttl_seconds"));
        }
    }

    @Override
    public OtpConfig updateConfig(OtpConfig config) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CONFIG)) {
            statement.setInt(1, config.codeLength());
            statement.setInt(2, config.ttlSeconds());
            statement.executeUpdate();
            return getConfig();
        }
    }
}
