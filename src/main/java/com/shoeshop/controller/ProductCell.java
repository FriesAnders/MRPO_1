package com.shoeshop.controller;

import com.shoeshop.model.Product;
import com.shoeshop.util.ImageUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Кастомная ячейка списка товаров.
 *
 * Макет строки:
 *   [Фото] | [Категория | Наименование (жирный) / Описание / Производитель /
 *             Поставщик / Цена (с перечёркиванием при скидке) /
 *             Единица измерения / Количество на складе] | [Действующая скидка]
 *
 * Подсветка фона:
 *   - скидка > 15 %  → #2E8B57 (тёмно-зелёный)
 *   - товар на складе = 0 → #ADD8E6 (голубой)
 *   - иначе → белый
 */
public class ProductCell extends ListCell<Product> {

    private static final Font FONT_BOLD   = Font.font("Times New Roman", FontWeight.BOLD, 14);
    private static final Font FONT_NORMAL = Font.font("Times New Roman", 13);
    private static final Font FONT_SMALL  = Font.font("Times New Roman", 11);

    @Override
    protected void updateItem(Product product, boolean empty) {
        super.updateItem(product, empty);

        if (empty || product == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
            return;
        }

        // ── Левая часть: фото ──────────────────────────────────────────────
        ImageView imageView = new ImageView(ImageUtil.loadProductImage(product.getImagePath()));
        imageView.setFitWidth(120);
        imageView.setFitHeight(100);
        imageView.setPreserveRatio(true);
        imageView.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1;");

        // ── Центр: текстовая информация ───────────────────────────────────
        VBox infoBox = new VBox(3);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label titleLabel    = new Label(product.getCategoryName() + " | " + product.getName());
        titleLabel.setFont(FONT_BOLD);
        titleLabel.setWrapText(true);

        Label descLabel     = makeInfo("Описание: ",     product.getDescription());
        Label mfrLabel      = makeInfo("Производитель: ", product.getManufacturerName());
        Label supplierLabel = makeInfo("Поставщик: ",    product.getSupplierName());
        Label unitLabel     = makeInfo("Ед. измерения: ", product.getUnitName());
        Label stockLabel    = makeInfo("На складе: ",    String.valueOf(product.getStockQuantity()));

        HBox priceBox = buildPriceBox(product);

        infoBox.getChildren().addAll(
                titleLabel, descLabel, mfrLabel, supplierLabel,
                priceBox, unitLabel, stockLabel);

        // ── Правая часть: скидка ──────────────────────────────────────────
        VBox discountBox = buildDiscountBox(product);

        // ── Корневой контейнер ────────────────────────────────────────────
        HBox root = new HBox(10, imageView, infoBox, discountBox);
        root.setPadding(new Insets(8));
        root.setAlignment(Pos.CENTER_LEFT);

        // ── Цвет фона и текста в зависимости от условий ──────────────────
        applyRowStyle(root, product, titleLabel, descLabel, mfrLabel,
                supplierLabel, unitLabel, stockLabel);

        setGraphic(root);
        setText(null);
        setPadding(Insets.EMPTY);
    }

    /** Строит блок цены (с перечёркиванием, если есть скидка). */
    private HBox buildPriceBox(Product product) {
        HBox priceBox = new HBox(6);
        priceBox.setAlignment(Pos.CENTER_LEFT);

        if (product.hasDiscount()) {
            // Основная цена: перечёркнутая, красная
            Label original = new Label(String.format("Цена: %.2f руб.", product.getPrice()));
            original.setFont(FONT_NORMAL);
            // Label не имеет setStrikethrough — применяем через CSS
            original.setStyle("-fx-strikethrough: true;");
            original.setTextFill(Color.RED);

            // Цена со скидкой: чёрная
            Label discounted = new Label(String.format("%.2f руб.", product.getDiscountedPrice()));
            discounted.setFont(FONT_NORMAL);
            discounted.setTextFill(Color.BLACK);

            priceBox.getChildren().addAll(original, discounted);
        } else {
            Label priceLabel = makeInfo("Цена: ",
                    String.format("%.2f руб.", product.getPrice()));
            priceBox.getChildren().add(priceLabel);
        }

        return priceBox;
    }

    /** Строит правый блок «Действующая скидка». */
    private VBox buildDiscountBox(Product product) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(110);
        box.setMinWidth(110);
        box.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; " +
                     "-fx-padding: 6; -fx-background-color: #f9f9f9;");

        Label caption = new Label("Действующая\nскидка");
        caption.setFont(FONT_SMALL);
        caption.setTextAlignment(TextAlignment.CENTER);
        caption.setWrapText(true);

        String discountText = product.hasDiscount()
                ? String.format("%.0f%%", product.getDiscount())
                : "—";
        Label valueLabel = new Label(discountText);
        valueLabel.setFont(FONT_BOLD);
        valueLabel.setTextAlignment(TextAlignment.CENTER);

        box.getChildren().addAll(caption, valueLabel);
        return box;
    }

    /** Применяет цвет фона строки и при необходимости белый цвет текста. */
    private void applyRowStyle(HBox root, Product product,
                               Label... textLabels) {
        if (product.isHighDiscount()) {
            // Скидка > 15 % — тёмно-зелёный фон, белый текст
            root.setStyle("-fx-background-color: #2E8B57; " +
                          "-fx-border-color: #1a6e40; -fx-border-width: 0 0 1 0;");
            for (Label lbl : textLabels) {
                // Не перекрашиваем красные метки цены
                if (!Color.RED.equals(lbl.getTextFill())) {
                    lbl.setTextFill(Color.WHITE);
                }
            }
        } else if (product.isOutOfStock()) {
            // Нет на складе — голубой фон
            root.setStyle("-fx-background-color: #ADD8E6; " +
                          "-fx-border-color: #8ab5c4; -fx-border-width: 0 0 1 0;");
        } else {
            root.setStyle("-fx-background-color: white; " +
                          "-fx-border-color: #dddddd; -fx-border-width: 0 0 1 0;");
        }
    }

    /** Создаёт информационную метку вида «Поле: Значение». */
    private Label makeInfo(String caption, String value) {
        Label label = new Label(caption + (value != null ? value : ""));
        label.setFont(FONT_NORMAL);
        label.setWrapText(true);
        return label;
    }
}
