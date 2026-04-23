package com.shoeshop.controller;

import com.shoeshop.model.Order;
import com.shoeshop.model.OrderStatus;
import com.shoeshop.service.OrderService;
import com.shoeshop.util.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Контроллер формы добавления / редактирования заказа. */
public class OrderFormController {

    @FXML private Label                 idLabel;
    @FXML private TextField             articleField;
    @FXML private ComboBox<OrderStatus> statusCombo;
    @FXML private TextArea              addressArea;
    @FXML private DatePicker            orderDatePicker;
    @FXML private DatePicker            deliveryDatePicker;

    private Stage             stage;
    private Order             currentOrder;  // null = добавление
    private OrderListController listController;

    private final OrderService orderService = new OrderService();

    @FXML
    public void initialize() {
        loadStatuses();
        // Дата заказа по умолчанию — сегодня (при добавлении)
        orderDatePicker.setValue(LocalDate.now());
    }

    /**
     * Вызывается из OrderListController перед отображением формы.
     *
     * @param order null — добавление, не null — редактирование
     */
    public void setOrder(Order order, OrderListController listCtrl) {
        this.currentOrder  = order;
        this.listController = listCtrl;

        if (order != null) {
            fillFieldsFromOrder(order);
        } else {
            idLabel.setText("ID: авто");
        }
    }

    public void setStage(Stage stage) { this.stage = stage; }

    // ── Обработчики кнопок ────────────────────────────────────────────────

    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            Order order = buildOrderFromFields();

            if (currentOrder == null) {
                orderService.save(order);
                AlertUtil.showInfo("Заказ добавлен",
                        "Заказ «" + order.getArticle() + "» успешно добавлен.");
            } else {
                order.setId(currentOrder.getId());
                orderService.update(order);
                AlertUtil.showInfo("Заказ обновлён",
                        "Заказ «" + order.getArticle() + "» успешно обновлён.");
            }

            listController.loadOrders();
            stage.close();

        } catch (SQLException e) {
            AlertUtil.showError("Ошибка сохранения",
                    "Не удалось сохранить заказ.\n\n" + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }

    // ── Вспомогательные методы ─────────────────────────────────────────────

    private void loadStatuses() {
        try {
            List<OrderStatus> statuses = orderService.findAllStatuses();
            statusCombo.getItems().setAll(statuses);
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка", "Не удалось загрузить статусы заказов.\n" + e.getMessage());
        }
    }

    private void fillFieldsFromOrder(Order o) {
        idLabel.setText("ID: " + o.getId());
        articleField.setText(o.getArticle());
        addressArea.setText(o.getPickupAddress() != null ? o.getPickupAddress() : "");
        orderDatePicker.setValue(o.getOrderDate());
        deliveryDatePicker.setValue(o.getDeliveryDate());

        // Выбираем статус по id
        for (OrderStatus status : statusCombo.getItems()) {
            if (status.getId() == o.getStatusId()) {
                statusCombo.setValue(status);
                break;
            }
        }
    }

    private Order buildOrderFromFields() {
        Order o = new Order();
        o.setArticle(articleField.getText().trim());
        o.setStatusId(statusCombo.getValue().getId());
        o.setPickupAddress(addressArea.getText().trim());
        o.setOrderDate(orderDatePicker.getValue());
        o.setDeliveryDate(deliveryDatePicker.getValue());
        return o;
    }

    /** Проверяет корректность заполнения формы заказа. */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (articleField.getText().isBlank()) {
            errors.append("• Артикул заказа обязателен для заполнения.\n");
        } else {
            // Проверяем уникальность артикула
            try {
                int excludeId = currentOrder != null ? currentOrder.getId() : 0;
                if (orderService.isArticleExists(articleField.getText().trim(), excludeId)) {
                    errors.append("• Заказ с таким артикулом уже существует.\n");
                }
            } catch (SQLException e) {
                errors.append("• Ошибка проверки артикула: ").append(e.getMessage()).append("\n");
            }
        }

        if (statusCombo.getValue() == null) {
            errors.append("• Выберите статус заказа.\n");
        }
        if (addressArea.getText().isBlank()) {
            errors.append("• Адрес пункта выдачи обязателен для заполнения.\n");
        }
        if (orderDatePicker.getValue() == null) {
            errors.append("• Укажите дату заказа.\n");
        }

        // Дата доставки не может быть раньше даты заказа
        if (orderDatePicker.getValue() != null && deliveryDatePicker.getValue() != null) {
            if (deliveryDatePicker.getValue().isBefore(orderDatePicker.getValue())) {
                errors.append("• Дата доставки не может быть раньше даты заказа.\n");
            }
        }

        if (!errors.isEmpty()) {
            AlertUtil.showError("Ошибки заполнения формы",
                    "Пожалуйста, исправьте следующие ошибки:\n\n" + errors);
            return false;
        }
        return true;
    }
}
