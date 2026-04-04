package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** DAO для работы с таблицей users. */
public class UserDao {

    private static final String FIND_BY_LOGIN = """
            SELECT u.id, u.login, u.password_hash, u.role_id,
                   r.name AS role_name,
                   u.last_name, u.first_name, u.middle_name
            FROM users u
            JOIN roles r ON u.role_id = r.id
            WHERE u.login = ?
            """;

    /**
     * Ищет пользователя по логину.
     *
     * @return Optional с пользователем, или empty, если не найден.
     */
    public Optional<User> findByLogin(String login) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(FIND_BY_LOGIN)) {

            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    // Маппинг строки ResultSet → объект User
    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRoleId(rs.getInt("role_id"));
        user.setRoleName(rs.getString("role_name"));
        user.setLastName(rs.getString("last_name"));
        user.setFirstName(rs.getString("first_name"));
        user.setMiddleName(rs.getString("middle_name"));
        return user;
    }
}
