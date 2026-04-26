package org.example.codeconfirmapp.dao;

import org.example.codeconfirmapp.model.OtpConfig;

import java.sql.SQLException;

public interface OtpConfigDao {
    OtpConfig getConfig() throws SQLException;

    OtpConfig updateConfig(OtpConfig config) throws SQLException;
}
