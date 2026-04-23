package com.shoeshop.controller;

import com.shoeshop.model.Product;
import com.shoeshop.model.Supplier;
import com.shoeshop.service.ProductService;
import com.shoeshop.util.AlertUtil;
import com.shoeshop.util.SceneManager;
import com.shoeshop.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

/** Контроллер экрана «Список товаров». Работает для всех ролей. */
public class ProductListController {

    // ── Заголовок ──────────────────────────────────────────────────────────
    @FXML private ImageView logoView;
    @FXML private Label     userNameLabel;

    // ── Панель фильтров (видна менеджеру и администратору) ────────────────
    @FXML private HBox            filterBar;
    @FXML private TextField       searchField;
    @FXML private ComboBox<Supplier> supplierFilterCombo;
    @FXML private ComboBox<String>   sortCombo;

    // ── Список товаров ─────────────────────────────────────────────────────
    @FXML private ListView<Product> productListView;

    // ── Кнопки действий ────────────────────────────────────────────────────
    @FXML private Button addProductButton;
    @FXML private Button deleteProductButton;
    @FXML private Button ordersButton;

    private final ProductService productService = new ProductService();

    // Исходный список для FilteredList / SortedList
    private ObservableList<Product> allProducts;
    private FilteredList<Product>   filteredProducts;

    @FXML
    public void initialize() {
        loadLogo();
        userNameLabel.setText(SessionManager.getDisplayName());

        setupRoleBasedVisibility();
        loadProducts();

        if (SessionManager.isManagerOrAdmin()) {
            setupFilterAndSort();
        }

        // Двойной клик — открытие формы редактирования (только для администратора)
        productListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && SessionManager.isAdmin()) {
                Product selected = productListView.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openProductForm(selected);
                }
            }
        });
    }

    // ── Загрузка данных ────────────────────────────────────────────────────

    /** Загружает (или перезагружает) список товаров из базы данных. */
    public void loadProducts() {
        try {
            List<Product> products = productService.findAll();
            allProducts = FXCollections.observableArrayList(products);

            filteredProducts = new FilteredList<>(allProducts, p -> true);
            SortedList<Product> sortedProducts = new SortedList<>(filteredProducts);

            productListView.setItems(sortedProducts);
            productListView.setCellFactory(lv -> new ProductCell());

            // Привязываем компаратор сортировки, если панель фильтров активна
            if (SessionManager.isManagerOrAdmin()) {
                sortedProducts.comparatorProperty().bind(
                        // Comparator задаётся через слушатель sortCombo
                        javafx.beans.binding.Bindings.createObjectBinding(
                                this::buildComparator, sortCombo.valueProperty()));
            }
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка загрузки товаров",
                    "Не удалось загрузить список товаров из базы данных.\n\n" + e.getMessage());
        }
    }

    // ── Настройка фильтров и сортировки ───────────────────────────────────

    private void setupFilterAndSort() {
        // Заполняем выпадающий список поставщиков
        try {
            List<Supplier> suppliers = productService.findAllSuppliers();
            ObservableList<Supplier> supplierItems = FXCollections.observableArrayList();

            // Первый элемент — «Все поставщики» (сбрасывает фильтр)
            Supplier allSuppliers = new Supplier(-1, "Все поставщики");
            supplierItems.add(allSuppliers);
            supplierItems.addAll(suppliers);

            supplierFilterCombo.setItems(supplierItems);
            supplierFilterCombo.setValue(allSuppliers);
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка", "Не удалось загрузить список поставщиков.\n" + e.getMessage());
        }

        sortCombo.setItems(FXCollections.observableArrayList(
                "По умолчанию",
                "По количеству ↑",
                "По количеству ↓"));
        sortCombo.setValue("По умолчанию");

        // Слушатели поиска и фильтра — обновляют предикат в реальном времени
        searchField.textProperty().addListener((obs, old, val) -> applyFilter());
        supplierFilterCombo.valueProperty().addListener((obs, old, val) -> applyFilter());
    }

    /** Формирует и применяет предикат фильтрации. */
    private void applyFilter() {
        String   searchText      = searchField.getText().toLowerCase().trim();
        Supplier selectedSupplier = supplierFilterCombo.getValue();

        filteredProducts.setPredicate(product -> {
            // 1. Фильтр по поставщику
            if (selectedSupplier != null && selectedSupplier.getId() != -1) {
                if (product.getSupplierId() != selectedSupplier.getId()) return false;
            }

            // 2. Текстовый поиск по нескольким атрибутам одновременно
            if (!searchText.isEmpty()) {
                return containsIgnoreCase(product.getName(), searchText) ||
                       containsIgnoreCase(product.getCategoryName(), searchText) ||
                       containsIgnoreCase(product.getManufacturerName(), searchText) ||
                       containsIgnoreCase(product.getSupplierName(), searchText) ||
                       containsIgnoreCase(product.getUnitName(), searchText) ||
                       containsIgnoreCase(product.getDescription(), searchText);
            }
            return true;
        });
    }

    /** Строит компаратор на основе выбранного варианта сортировки. */
    private Comparator<Product> buildComparator() {
        String selected = sortCombo.getValue();
        if ("По количеству ↑".equals(selected)) {
            return Comparator.comparingInt(Product::getStockQuantity);
        } else if ("По количеству ↓".equals(selected)) {
            return Comparator.comparingInt(Product::getStockQuantity).reversed();
        }
        return null; // Сортировка по умолчанию (по id)
    }

    // ── Обработчики кнопок ────────────────────────────────────────────────

    @FXML
    private void handleAddProduct() {
        openProductForm(null); // null = режим добавления
    }

    @FXML
    private void handleDeleteProduct() {
        Product selected = productListView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showWarning("Товар не выбран",
                    "Выберите товар в списке, который хотите удалить.");
            return;
        }

        boolean confirmed = AlertUtil.showConfirmation(
                "Удаление товара",
                "Вы уверены, что хотите удалить товар «" + selected.getName() + "»?\n" +
                "Это действие невозможно отменить.");
        if (!confirmed) return;

        try {
            productService.delete(selected.getId());
            loadProducts(); // Обновляем список после удаления
            AlertUtil.showInfo("Товар удалён", "Товар «" + selected.getName() + "» успешно удалён.");
        } catch (IllegalStateException e) {
            AlertUtil.showError("Удаление невозможно", e.getMessage());
        } catch (SQLException e) {
            AlertUtil.showError("Ошибка удаления",
                    "Не удалось удалить товар.\n\n" + e.getMessage());
        }
    }

    @FXML
    private void handleOrders() {
        try {
            SceneManager.showOrderList();
        } catch (IOException e) {
            AlertUtil.showError("Ошибка навигации",
                    "Не удалось открыть список заказов.\n" + e.getMessage());
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

    // ── Форма товара ───────────────────────────────────────────────────────

    /**
     * Открывает модальное окно формы товара.
     *
     * @param product null — режим добавления, не null — режим редактирования.
     */
    private void openProductForm(Product product) {
        // Запрещаем одновременно открытые окна редактирования
        if (!ProductFormController.tryOpen()) {
            AlertUtil.showWarning("Окно редактирования уже открыто",
                    "Закройте текущее окно редактирования перед открытием нового.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/product_form.fxml"));
            Parent root = loader.load();

            ProductFormController controller = loader.getController();
            controller.setProduct(product, this);

            Stage formStage = new Stage();
            formStage.initModality(Modality.APPLICATION_MODAL);
            formStage.initOwner(SceneManager.getPrimaryStage());
            formStage.setTitle((product == null ? "Добавление" : "Редактирование") +
                               " товара — ООО «Обувь»");
            formStage.setMinWidth(700);
            formStage.setMinHeight(600);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());
            formStage.setScene(scene);

            controller.setStage(formStage);
            formStage.showAndWait();

        } catch (IOException e) {
            ProductFormController.release(); // Снимаем блокировку при ошибке
            AlertUtil.showError("Ошибка открытия формы",
                    "Не удалось открыть форму товара.\n" + e.getMessage());
        }
    }

    // ── Вспомогательные методы ─────────────────────────────────────────────

    /** Настраивает видимость элементов в зависимости от роли пользователя. */
    private void setupRoleBasedVisibility() {
        boolean isManagerOrAdmin = SessionManager.isManagerOrAdmin();
        boolean isAdmin          = SessionManager.isAdmin();

        filterBar.setVisible(isManagerOrAdmin);
        filterBar.setManaged(isManagerOrAdmin);

        addProductButton.setVisible(isAdmin);
        addProductButton.setManaged(isAdmin);

        deleteProductButton.setVisible(isAdmin);
        deleteProductButton.setManaged(isAdmin);

        ordersButton.setVisible(isManagerOrAdmin);
        ordersButton.setManaged(isManagerOrAdmin);
    }

    private void loadLogo() {
        InputStream is = getClass().getResourceAsStream("/images/logo.png");
        if (is != null) {
            logoView.setImage(new Image(is));
        }
    }

    private boolean containsIgnoreCase(String text, String search) {
        return text != null && text.toLowerCase().contains(search);
    }
}
