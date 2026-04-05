package com.shoeshop.controller;

import com.shoeshop.model.User;
import com.shoeshop.service.AuthService;
import com.shoeshop.util.AlertUtil;
import com.shoeshop.util.SceneManager;
import com.shoeshop.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;

/** Контроллер экрана входа в систему. */
public class LoginController {

    @FXML private ImageView    logoView;
    @FXML private TextField    loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label        errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        // Загружаем логотип компании из ресурсов
        InputStream logoStream = getClass().getResourceAsStream("/images/logo.png");
        if (logoStream != null) {
            logoView.setImage(new Image(logoStream));
        }
        errorLabel.setVisible(false);
    }

    /** Обрабатывает нажатие кнопки «Войти». */
    @FXML
    private void handleLogin() {
        String login    = loginField.getText().trim();
        String password = passwordField.getText();

        // Валидация: проверяем, что поля заполнены
        if (login.isEmpty() || password.isEmpty()) {
            showError("Заполните поля «Логин» и «Пароль».");
            return;
        }

        try {
            Optional<User> userOpt = authService.login(login, password);
            if (userOpt.isEmpty()) {
                showError("Неверный логин или пароль. Проверьте введённые данные.");
                passwordField.clear();
                return;
            }

            // Авторизация успешна — сохраняем пользователя и переходим к товарам
            SessionManager.setCurrentUser(userOpt.get());
            SceneManager.showProductList();

        } catch (SQLException e) {
            AlertUtil.showError("Ошибка базы данных",
                    "Не удалось подключиться к базе данных.\n" +
                    "Убедитесь, что PostgreSQL запущен и параметры в db.properties верны.\n\n" +
                    "Детали: " + e.getMessage());
        } catch (Exception e) {
            AlertUtil.showError("Ошибка", "Произошла непредвиденная ошибка: " + e.getMessage());
        }
    }

    /** Переход в режим гостя: пользователь не авторизован, доступ только к просмотру товаров. */
    @FXML
    private void handleGuest() {
        try {
            SessionManager.logout(); // currentUser = null → режим гостя
            SceneManager.showProductList();
        } catch (Exception e) {
            AlertUtil.showError("Ошибка", "Не удалось открыть список товаров: " + e.getMessage());
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
