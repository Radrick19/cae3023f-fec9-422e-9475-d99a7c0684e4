package org.example.codeconfirmapp.dao;

import org.example.codeconfirmapp.model.OtpCode;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public interface OtpCodeDao {
    void save(OtpCode otpCode) throws SQLException;

    Optional<OtpCode> findLatestActiveByUserAndOperation(UUID userId, String operationId) throws SQLException;

    void updateStatus(UUID id, String status) throws SQLException;

    int expireOlderThanNow() throws SQLException;

    void deleteByUserId(UUID userId) throws SQLException;
}
