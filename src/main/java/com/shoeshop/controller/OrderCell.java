package com.shoeshop.controller;

import com.shoeshop.model.Order;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.time.format.DateTimeFormatter;

/**
 * Кастомная ячейка списка заказов.
 *
 * Макет строки:
 *   [Артикул заказа (жирный) / Статус / Адрес пункта выдачи / Дата заказа]
 *   | [Дата доставки] (правый блок с рамкой)
 */
public class OrderCell extends ListCell<Order> {

    private static final Font FONT_BOLD   = Font.font("Times New Roman", FontWeight.BOLD, 14);
    private static final Font FONT_NORMAL = Font.font("Times New Roman", 13);
    private static final Font FONT_SMALL  = Font.font("Times New Roman", 11);

    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    protected void updateItem(Order order, boolean empty) {
        super.updateItem(order, empty);

        if (empty || order == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
            return;
        }

        // ── Левая часть: основная информация ─────────────────────────────
        VBox infoBox = new VBox(4);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label articleLabel = new Label(order.getArticle());
        articleLabel.setFont(FONT_BOLD);

        Label statusLabel  = new Label("Статус: " + order.getStatusName());
        statusLabel.setFont(FONT_NORMAL);

        Label addressLabel = new Label("Адрес пункта выдачи: " + order.getPickupAddress());
        addressLabel.setFont(FONT_NORMAL);
        addressLabel.setWrapText(true);

        String orderDateStr = order.getOrderDate() != null
                ? order.getOrderDate().format(DATE_FMT) : "—";
        Label dateLabel = new Label("Дата заказа: " + orderDateStr);
        dateLabel.setFont(FONT_NORMAL);

        infoBox.getChildren().addAll(articleLabel, statusLabel, addressLabel, dateLabel);

        // ── Правая часть: дата доставки ───────────────────────────────────
        VBox deliveryBox = new VBox(4);
        deliveryBox.setAlignment(Pos.CENTER);
        deliveryBox.setPrefWidth(130);
        deliveryBox.setMinWidth(130);
        deliveryBox.setStyle("-fx-border-color: #aaaaaa; -fx-border-width: 1; " +
                             "-fx-padding: 8; -fx-background-color: #fafafa;");

        Label deliveryCaption = new Label("Дата доставки");
        deliveryCaption.setFont(FONT_SMALL);
        deliveryCaption.setTextAlignment(TextAlignment.CENTER);
        deliveryCaption.setWrapText(true);

        String deliveryDateStr = order.getDeliveryDate() != null
                ? order.getDeliveryDate().format(DATE_FMT) : "не задана";
        Label deliveryValue = new Label(deliveryDateStr);
        deliveryValue.setFont(FONT_BOLD);
        deliveryValue.setTextAlignment(TextAlignment.CENTER);

        deliveryBox.getChildren().addAll(deliveryCaption, deliveryValue);

        // ── Корневой контейнер ────────────────────────────────────────────
        HBox root = new HBox(10, infoBox, deliveryBox);
        root.setPadding(new Insets(8));
        root.setAlignment(Pos.CENTER_LEFT);
        root.setStyle("-fx-background-color: white; " +
                      "-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");

        setGraphic(root);
        setText(null);
        setPadding(Insets.EMPTY);
    }
}
