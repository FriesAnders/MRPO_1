package com.shoeshop.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Управляет переключением сцен в главном окне приложения.
 * Форма товара и форма заказа открываются как отдельные модальные окна
 * через ProductFormController и OrderFormController соответственно.
 */
public final class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {}

    /** Инициализируется один раз из Main.start(). */
    public static void init(Stage stage) {
        primaryStage = stage;
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);

        // Устанавливаем иконку приложения, если она есть в ресурсах
        InputStream iconStream = SceneManager.class.getResourceAsStream("/images/icon.png");
        if (iconStream != null) {
            primaryStage.getIcons().add(new Image(iconStream));
        }
    }

    /** Показывает экран входа. */
    public static void showLogin() throws IOException {
        loadScene("/fxml/login.fxml", "Вход в систему");
    }

    /** Показывает список товаров. */
    public static void showProductList() throws IOException {
        loadScene("/fxml/product_list.fxml", "Список товаров");
    }

    /** Показывает список заказов. */
    public static void showOrderList() throws IOException {
        loadScene("/fxml/order_list.fxml", "Список заказов");
    }

    public static Stage getPrimaryStage() { return primaryStage; }

    // Загружает FXML и заменяет сцену в главном окне
    private static void loadScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        String cssUrl = SceneManager.class.getResource("/css/styles.css").toExternalForm();
        scene.getStylesheets().add(cssUrl);

        primaryStage.setScene(scene);
        primaryStage.setTitle(title + " — ООО «Обувь»");
        primaryStage.show();
    }
}
