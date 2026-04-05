package com.shoeshop;

import com.shoeshop.util.AlertUtil;
import com.shoeshop.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Точка входа в приложение ООО «Обувь».
 *
 * Запуск: mvn javafx:run
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        SceneManager.init(primaryStage);
        try {
            SceneManager.showLogin();
        } catch (Exception e) {
            AlertUtil.showError("Ошибка запуска",
                    "Не удалось загрузить экран входа.\n\n" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
