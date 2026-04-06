package com.shoeshop.controller;

import com.shoeshop.model.Order;
import com.shoeshop.service.OrderService;
import com.shoeshop.util.AlertUtil;
import com.shoeshop.util.SceneManager;
import com.shoeshop.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/** Контроллер экрана «Список заказов» (менеджер и администратор). */
public class OrderListController {

    @FXML private Label         userNameLabel;
    @FXML private ListView<Order> orderListView;
    @FXML private Button        addOrderButton;
    @FXML private Button        deleteOrderButton;

    private final OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        userNameLabel.setText(SessionManager.getDisplayName());
        setupRoleBasedVisibility();
        loadOrders();

        // Двойной клик — открытие формы редактирования (только администратор)
        orderListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && SessionManager.isAdmin()) {
                Order selected = orderListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openOrderForm(selected);
                }
            }
        });
    }

    /** Загружает (или перезагружает) список заказов. */
    public void loadOrders() {
        try {
            List<Order> orders = orderService.findAll();
            orderListView.setItems(FXCollections.observableArrayList(orders));
            orderListView.setCellFactory(lv -> new OrderCell());
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка загрузки заказов",
                    "Не удалось загрузить список заказов из базы данных.\n\n" + e.getMessage());
        }
    }

    @FXML
    private void handleAddOrder() {
        openOrderForm(null); // null = режим добавления
    }

    @FXML
    private void handleDeleteOrder() {
        Order selected = orderListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Заказ не выбран",
                    "Выберите заказ в списке, который хотите удалить.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Удаление заказа",
                "Вы уверены, что хотите удалить заказ «" + selected.getArticle() + "»?\n" +
                "Все позиции заказа также будут удалены. Это действие невозможно отменить.");
        if (!confirmed) return;

        try {
            orderService.delete(selected.getId());
            loadOrders();
            AlertUtil.showInfo("Заказ удалён",
                    "Заказ «" + selected.getArticle() + "» успешно удалён.");
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка удаления",
                    "Не удалось удалить заказ.\n\n" + e.getMessage());
        }
    }

    /** Возврат к списку товаров. */
    @FXML
    private void handleBack() {
        try {
            SceneManager.showProductList();
        } catch (IOException e) {
            AlertUtil.showError("Ошибка навигации",
                    "Не удалось вернуться к списку товаров.\n" + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        try {
            SceneManager.showLogin();
        } catch (IOException e) {
            AlertUtil.showError("Ошибка", "Не удалось вернуться на экран входа.");
        }
    }

    // ── Форма заказа ───────────────────────────────────────────────────────

    private void openOrderForm(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/order_form.fxml"));
            Parent root = loader.load();

            OrderFormController controller = loader.getController();
            controller.setOrder(order, this);

            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.initOwner(SceneManager.getPrimaryStage());
            formStage.setTitle((order == null ? "Добавление" : "Редактирование") +
                               " заказа — ООО «Обувь»");
            formStage.setMinWidth(550);
            formStage.setMinHeight(450);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            formStage.setScene(scene);

            controller.setStage(formStage);
            formStage.showAndWait();

        } catch (IOException e) {
            AlertUtil.showError("Ошибка открытия формы",
                    "Не удалось открыть форму заказа.\n" + e.getMessage());
        }
    }

    private void setupRoleBasedVisibility() {
        boolean isAdmin = SessionManager.isAdmin();
        addOrderButton.setVisible(isAdmin);
        addOrderButton.setManaged(isAdmin);
        deleteOrderButton.setVisible(isAdmin);
        deleteOrderButton.setManaged(isAdmin);
    }
}
