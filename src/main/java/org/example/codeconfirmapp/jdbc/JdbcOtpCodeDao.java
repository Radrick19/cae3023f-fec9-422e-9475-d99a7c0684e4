package org.example.codeconfirmapp.jdbc;

import org.example.codeconfirmapp.dao.OtpCodeDao;
import org.example.codeconfirmapp.model.DeliveryChannel;
import org.example.codeconfirmapp.model.OtpCode;
import org.example.codeconfirmapp.model.OtpStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class JdbcOtpCodeDao implements OtpCodeDao {
    private static final String OTP_COLUMNS =
            "id, user_id, operation_id, code_hash, status, delivery_channel, destination, created_at, expires_at";

    private static final String INSERT_OTP = """
            INSERT INTO otp_codes
            (id, user_id, operation_id, code_hash, status, delivery_channel, destination, created_at, expires_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SELECT_ACTIVE_BY_USER_AND_OPERATION = """
            SELECT %s
            FROM otp_codes
            WHERE user_id = ? AND operation_id = ? AND status = 'ACTIVE'
            ORDER BY created_at DESC
            LIMIT 1
            """.formatted(OTP_COLUMNS);

    private final DataSource dataSource;

    public JdbcOtpCodeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(OtpCode otpCode) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_OTP)) {
            statement.setObject(1, otpCode.id());
            statement.setObject(2, otpCode.userId());
            statement.setString(3, otpCode.operationId());
            statement.setString(4, otpCode.codeHash());
            statement.setString(5, otpCode.status().name());
            statement.setString(6, otpCode.deliveryChannel().name());
            statement.setString(7, otpCode.destination());
            statement.setTimestamp(8, Timestamp.from(otpCode.createdAt()));
            statement.setTimestamp(9, Timestamp.from(otpCode.expiresAt()));
            statement.executeUpdate();
        }
    }

    @Override
    public Optional<OtpCode> findLatestActiveByUserAndOperation(UUID userId, String operationId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ACTIVE_BY_USER_AND_OPERATION)) {
            statement.setObject(1, userId);
            statement.setString(2, operationId);
            return findOne(statement);
        }
    }

    @Override
    public void updateStatus(UUID id, String status) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE otp_codes SET status = ? WHERE id = ?
                     """)) {
            statement.setString(1, status);
            statement.setObject(2, id);
            statement.executeUpdate();
        }
    }

    @Override
    public int expireOlderThanNow() throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     UPDATE otp_codes
                     SET status = 'EXPIRED'
                     WHERE status = 'ACTIVE' AND expires_at <= CURRENT_TIMESTAMP
                     """)) {
            return statement.executeUpdate();
        }
    }

    @Override
    public void deleteByUserId(UUID userId) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     DELETE FROM otp_codes WHERE user_id = ?
                     """)) {
            statement.setObject(1, userId);
            statement.executeUpdate();
        }
    }

    private OtpCode map(ResultSet resultSet) throws SQLException {
        return new OtpCode(
                resultSet.getObject("id", UUID.class),
                resultSet.getObject("user_id", UUID.class),
                resultSet.getString("operation_id"),
                resultSet.getString("code_hash"),
                OtpStatus.valueOf(resultSet.getString("status")),
                DeliveryChannel.valueOf(resultSet.getString("delivery_channel")),
                resultSet.getString("destination"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("expires_at").toInstant()
        );
    }

    private Optional<OtpCode> findOne(PreparedStatement statement) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? Optional.of(map(resultSet)) : Optional.empty();
        }
    }
}
