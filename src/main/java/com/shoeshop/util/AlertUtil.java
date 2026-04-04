package com.shoeshop.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Фабрика диалоговых окон с информативными сообщениями.
 * Типы Alert соответствуют стандартным JavaFX:
 *   INFORMATION — информация
 *   WARNING     — предупреждение
 *   ERROR       — ошибка
 *   CONFIRMATION — подтверждение необратимых действий
 */
public final class AlertUtil {

    private AlertUtil() {}

    /** Информационное сообщение. */
    public static void showInfo(String title, String message) {
        show(Alert.AlertType.INFORMATION, title, message);
    }

    /** Предупреждение (например, о необратимой операции). */
    public static void showWarning(String title, String message) {
        show(Alert.AlertType.WARNING, title, message);
    }

    /** Сообщение об ошибке с подробным описанием. */
    public static void showError(String title, String message) {
        show(Alert.AlertType.ERROR, title, message);
    }

    /**
     * Диалог подтверждения необратимого действия (Да / Нет).
     *
     * @return true, если пользователь нажал «ОК».
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
