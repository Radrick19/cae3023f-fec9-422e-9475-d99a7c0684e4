package org.example.codeconfirmapp.config;

import org.example.codeconfirmapp.dao.OtpCodeDao;
import org.example.codeconfirmapp.dao.OtpConfigDao;
import org.example.codeconfirmapp.dao.UserDao;
import org.example.codeconfirmapp.jdbc.JdbcOtpCodeDao;
import org.example.codeconfirmapp.jdbc.JdbcOtpConfigDao;
import org.example.codeconfirmapp.jdbc.JdbcUserDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DaoConfig {
    @Bean
    public UserDao userDao(DataSource dataSource) {
        return new JdbcUserDao(dataSource);
    }

    @Bean
    public OtpConfigDao otpConfigDao(DataSource dataSource) {
        return new JdbcOtpConfigDao(dataSource);
    }

    @Bean
    public OtpCodeDao otpCodeDao(DataSource dataSource) {
        return new JdbcOtpCodeDao(dataSource);
    }
}
