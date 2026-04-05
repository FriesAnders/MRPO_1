package com.shoeshop.service;

import com.shoeshop.dao.UserDao;
import com.shoeshop.model.User;
import com.shoeshop.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Сервис аутентификации.
 * Проверяет логин и пароль пользователя; возвращает объект User при успехе.
 */
public class AuthService {

    private final UserDao userDao = new UserDao();

    /**
     * Выполняет авторизацию.
     *
     * @param login    введённый логин
     * @param password введённый пароль (в открытом виде)
     * @return Optional с пользователем, если пара логин/пароль верна; иначе empty.
     * @throws SQLException при ошибке обращения к БД
     */
    public Optional<User> login(String login, String password) throws SQLException {
        Optional<User> userOpt = userDao.findByLogin(login);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        // Сравниваем хэш введённого пароля с хэшем из БД
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            return Optional.of(user);
        }
        return Optional.empty();
    }
}
