package com.shoeshop.controller;

import com.shoeshop.model.*;
import com.shoeshop.service.ProductService;
import com.shoeshop.util.AlertUtil;
import com.shoeshop.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Контроллер формы добавления / редактирования товара.
 * Открывается только администратором.
 *
 * Гарантия единственного открытого окна реализуется через статический флаг isOpen.
 */
public class ProductFormController {

    // ── Статический флаг: разрешает открытие только одного окна ──────────
    private static boolean isOpen = false;

    /** Пытается открыть окно; возвращает false, если оно уже открыто. */
    public static boolean tryOpen() {
        if (isOpen) return false;
        isOpen = true;
        return true;
    }

    /** Освобождает флаг при закрытии окна. */
    public static void release() { isOpen = false; }

    // ── FXML-поля ─────────────────────────────────────────────────────────
    @FXML private Label     idLabel;
    @FXML private ImageView photoView;
    @FXML private TextField nameField;
    @FXML private ComboBox<Category>     categoryCombo;
    @FXML private TextArea  descriptionArea;
    @FXML private ComboBox<Manufacturer> manufacturerCombo;
    @FXML private ComboBox<Supplier>     supplierCombo;
    @FXML private TextField priceField;
    @FXML private ComboBox<Unit>         unitCombo;
    @FXML private TextField stockField;
    @FXML private TextField discountField;

    // ── Состояние формы ───────────────────────────────────────────────────
    private Stage                parentListController;
    private ProductListController listController;
    private Stage                stage;
    private Product              currentProduct;    // null = режим добавления
    private String               selectedImagePath; // новый путь к фото (или null)

    private final ProductService productService = new ProductService();

    @FXML
    public void initialize() {
        loadComboBoxData();
    }

    /**
     * Вызывается из ProductListController перед показом окна.
     *
     * @param product null — добавление, не null — редактирование
     */
    public void setProduct(Product product, ProductListController listCtrl) {
        this.currentProduct  = product;
        this.listController  = listCtrl;

        if (product == null) {
            // Режим добавления
            showNextId();
            photoView.setImage(ImageUtil.loadPlaceholder());
        } else {
            // Режим редактирования — заполняем поля из объекта
            fillFieldsFromProduct(product);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        // При закрытии окна крестиком освобождаем флаг
        stage.setOnCloseRequest(e -> release());
    }

    // ── Обработчики кнопок ────────────────────────────────────────────────

    /** Открывает диалог выбора фото и сохраняет изображение. */
    @FXML
    private void handleChoosePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Выберите изображение товара");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = chooser.showOpenDialog(stage);
        if (file == null) return;

        try {
            // Если редактируем и уже было изображение — удаляем старое
            if (currentProduct != null && currentProduct.getImagePath() != null) {
                ImageUtil.deleteImage(currentProduct.getImagePath());
                currentProduct.setImagePath(null);
            }
            // Также удаляем предыдущее выбранное фото (если пользователь менял несколько раз)
            if (selectedImagePath != null) {
                ImageUtil.deleteImage(selectedImagePath);
            }

            String productName = nameField.getText().isBlank() ? "product" : nameField.getText();
            selectedImagePath  = ImageUtil.saveAndResize(file, productName);

            // Показываем превью нового изображения
            photoView.setImage(new Image(new File(selectedImagePath).toURI().toString(),
                    120, 100, true, true));

        } catch (Exception e) {
            AlertUtil.showError("Ошибка загрузки изображения",
                    "Не удалось сохранить изображение.\n\n" + e.getMessage());
        }
    }

    /** Сохраняет товар (вставка или обновление) и закрывает форму. */
    @FXML
    private void handleSave() {
        if (!validateForm()) return;

        try {
            Product product = buildProductFromFields();

            if (currentProduct == null) {
                productService.save(product);
                AlertUtil.showInfo("Товар добавлен",
                        "Товар «" + product.getName() + "» успешно добавлен.");
            } else {
                product.setId(currentProduct.getId());
                productService.update(product);
                AlertUtil.showInfo("Товар обновлён",
                        "Данные товара «" + product.getName() + "» успешно обновлены.");
            }

            listController.loadProducts(); // Обновляем список в родительском окне
            closeForm();

        } catch (SQLException e) {
            AlertUtil.showError("Ошибка сохранения",
                    "Не удалось сохранить товар.\n\n" + e.getMessage());
        }
    }

    /** Закрывает форму без сохранения. */
    @FXML
    private void handleCancel() {
        // Если новое фото было выбрано, но сохранение отменено — удаляем временный файл
        if (selectedImagePath != null && (currentProduct == null ||
                !selectedImagePath.equals(currentProduct.getImagePath()))) {
            ImageUtil.deleteImage(selectedImagePath);
        }
        closeForm();
    }

    // ── Вспомогательные методы ─────────────────────────────────────────────

    /** Загружает данные справочников в ComboBox-ы. */
    private void loadComboBoxData() {
        try {
            List<Category>     categories    = productService.findAllCategories();
            List<Manufacturer> manufacturers = productService.findAllManufacturers();
            List<Supplier>     suppliers     = productService.findAllSuppliers();
            List<Unit>         units         = productService.findAllUnits();

            categoryCombo.getItems().setAll(categories);
            manufacturerCombo.getItems().setAll(manufacturers);
            supplierCombo.getItems().setAll(suppliers);
            unitCombo.getItems().setAll(units);

        } catch (SQLException e) {
            AlertUtil.showError("Ошибка загрузки справочников",
                    "Не удалось загрузить данные справочников.\n\n" + e.getMessage());
        }
    }

    /** Заполняет поля формы данными выбранного товара (режим редактирования). */
    private void fillFieldsFromProduct(Product p) {
        idLabel.setText("ID: " + p.getId());

        // Устанавливаем фото
        photoView.setImage(ImageUtil.loadProductImage(p.getImagePath()));

        nameField.setText(p.getName());
        descriptionArea.setText(p.getDescription() != null ? p.getDescription() : "");
        priceField.setText(p.getPrice().toPlainString());
        stockField.setText(String.valueOf(p.getStockQuantity()));
        discountField.setText(p.getDiscount().toPlainString());

        // Выбираем соответствующие элементы в ComboBox-ах по id
        selectById(categoryCombo,     p.getCategoryId());
        selectById(manufacturerCombo, p.getManufacturerId());
        selectById(supplierCombo,     p.getSupplierId());
        selectById(unitCombo,         p.getUnitId());
    }

    /** Отображает следующий автоматический id в режиме добавления. */
    private void showNextId() {
        try {
            int nextId = productService.getNextProductId();
            idLabel.setText("ID: " + nextId + " (авто)");
        } catch (SQLException e) {
            idLabel.setText("ID: авто");
        }
    }

    /** Строит объект Product из значений полей формы. */
    private Product buildProductFromFields() {
        Product p = new Product();
        p.setName(nameField.getText().trim());
        p.setCategoryId(categoryCombo.getValue().getId());
        p.setManufacturerId(manufacturerCombo.getValue().getId());
        p.setSupplierId(supplierCombo.getValue().getId());
        p.setUnitId(unitCombo.getValue().getId());
        p.setDescription(descriptionArea.getText().trim());
        p.setPrice(new BigDecimal(priceField.getText().trim()));
        p.setDiscount(new BigDecimal(discountField.getText().trim()));
        p.setStockQuantity(Integer.parseInt(stockField.getText().trim()));

        // Приоритет пути к фото: новый выбранный → старый из БД → null
        if (selectedImagePath != null) {
            p.setImagePath(selectedImagePath);
        } else if (currentProduct != null) {
            p.setImagePath(currentProduct.getImagePath());
        }

        return p;
    }

    /**
     * Валидация полей формы.
     *
     * @return true, если все поля заполнены корректно.
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().isBlank()) {
            errors.append("• Наименование товара обязательно для заполнения.\n");
        }
        if (categoryCombo.getValue() == null) {
            errors.append("• Выберите категорию товара.\n");
        }
        if (manufacturerCombo.getValue() == null) {
            errors.append("• Выберите производителя.\n");
        }
        if (supplierCombo.getValue() == null) {
            errors.append("• Выберите поставщика.\n");
        }
        if (unitCombo.getValue() == null) {
            errors.append("• Выберите единицу измерения.\n");
        }

        // Цена: числовая, неотрицательная
        try {
            BigDecimal price = new BigDecimal(priceField.getText().trim());
            if (price.compareTo(BigDecimal.ZERO) < 0) {
                errors.append("• Цена не может быть отрицательной.\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Цена должна быть числом (например: 1500.00).\n");
        }

        // Количество: целое, неотрицательное
        try {
            int qty = Integer.parseInt(stockField.getText().trim());
            if (qty < 0) {
                errors.append("• Количество на складе не может быть отрицательным.\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Количество на складе должно быть целым числом.\n");
        }

        // Скидка: число от 0 до 100
        try {
            BigDecimal discount = new BigDecimal(discountField.getText().trim());
            if (discount.compareTo(BigDecimal.ZERO) < 0 ||
                    discount.compareTo(BigDecimal.valueOf(100)) > 0) {
                errors.append("• Скидка должна быть числом от 0 до 100.\n");
            }
        } catch (NumberFormatException e) {
            errors.append("• Скидка должна быть числом от 0 до 100 (например: 15.5).\n");
        }

        if (!errors.isEmpty()) {
            AlertUtil.showError("Ошибки заполнения формы",
                    "Пожалуйста, исправьте следующие ошибки:\n\n" + errors);
            return false;
        }
        return true;
    }

    /** Выбирает элемент ComboBox по его id (реализовано через перебор). */
    private <T extends Object> void selectById(ComboBox<T> combo, int targetId) {
        for (T item : combo.getItems()) {
            int id = switch (item) {
                case Category     c -> c.getId();
                case Manufacturer m -> m.getId();
                case Supplier     s -> s.getId();
                case Unit         u -> u.getId();
                default -> -1;
            };
            if (id == targetId) {
                combo.setValue(item);
                return;
            }
        }
    }

    private void closeForm() {
        release();
        stage.close();
    }
}
