package com.shoeshop.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Конфигурация пула соединений HikariCP.
 * Настройки подключения берутся из db.properties в classpath.
 * Класс инициализируется один раз при первом обращении (паттерн статической инициализации).
 */
public class DatabaseConfig {

    private static final HikariDataSource DATA_SOURCE;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DatabaseConfig.class.getResourceAsStream("/db.properties");
            if (is == null) {
                throw new IOException("Файл db.properties не найден в classpath");
            }
            props.load(is);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.user"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(5000);  // 5 секунд на подключение

            DATA_SOURCE = new HikariDataSource(config);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Не удалось инициализировать подключение к базе данных: " + e.getMessage(), e);
        }
    }

    private DatabaseConfig() {}

    /** Возвращает соединение из пула. Закрывать соединение через try-with-resources. */
    public static Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
