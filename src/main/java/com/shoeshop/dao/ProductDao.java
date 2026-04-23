package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO для таблицы products. */
public class ProductDao {

    // Базовый SELECT с JOIN-ами для получения всех связанных наименований
    private static final String SELECT_BASE = """
            SELECT p.id, p.name, p.description, p.price, p.discount,
                   p.image_path, p.stock_quantity,
                   p.category_id,     c.name AS category_name,
                   p.manufacturer_id, m.name AS manufacturer_name,
                   p.supplier_id,     s.name AS supplier_name,
                   p.unit_id,         u.name AS unit_name
            FROM products p
            JOIN categories    c ON p.category_id     = c.id
            JOIN manufacturers m ON p.manufacturer_id = m.id
            JOIN suppliers     s ON p.supplier_id     = s.id
            JOIN units         u ON p.unit_id         = u.id
            """;

    /** Возвращает все товары, упорядоченные по id. */
    public List<Product> findAll() throws SQLException {
        List<Product> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_BASE + "ORDER BY p.id")) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Находит товар по id. */
    public Optional<Product> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE p.id = ?")) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    /**
     * Добавляет новый товар в базу данных.
     * ID генерируется автоматически (SERIAL); после вставки проставляется в объект.
     */
    public void insert(Product product) throws SQLException {
        String sql = """
                INSERT INTO products
                    (name, category_id, manufacturer_id, supplier_id, unit_id,
                     description, price, discount, image_path, stock_quantity)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            fillStatement(stmt, product);
            stmt.executeUpdate();

            // Получаем сгенерированный id и проставляем в объект
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                product.setId(keys.getInt(1));
            }
        }
    }

    /** Обновляет данные существующего товара. */
    public void update(Product product) throws SQLException {
        String sql = """
                UPDATE products SET
                    name            = ?,
                    category_id     = ?,
                    manufacturer_id = ?,
                    supplier_id     = ?,
                    unit_id         = ?,
                    description     = ?,
                    price           = ?,
                    discount        = ?,
                    image_path      = ?,
                    stock_quantity  = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillStatement(stmt, product);
            stmt.setInt(11, product.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Удаляет товар по id.
     * Перед вызовом убедитесь, что товар не входит ни в один заказ
     * (см. isUsedInOrders).
     */
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM products WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Проверяет, встречается ли товар в таблице order_items.
     * Если да — удаление запрещено.
     */
    public boolean isUsedInOrders(int productId) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM order_items WHERE product_id = ? LIMIT 1")) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    /** Возвращает максимальный id в таблице (для отображения следующего id при добавлении). */
    public int getNextId() throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT COALESCE(MAX(id), 0) + 1 FROM products")) {

            rs.next();
            return rs.getInt(1);
        }
    }

    // Заполняет PreparedStatement полями товара (без id)
    private void fillStatement(PreparedStatement stmt, Product p) throws SQLException {
        stmt.setString(1, p.getName());
        stmt.setInt(2, p.getCategoryId());
        stmt.setInt(3, p.getManufacturerId());
        stmt.setInt(4, p.getSupplierId());
        stmt.setInt(5, p.getUnitId());
        stmt.setString(6, p.getDescription());
        stmt.setBigDecimal(7, p.getPrice());
        stmt.setBigDecimal(8, p.getDiscount());
        stmt.setString(9, p.getImagePath());
        stmt.setInt(10, p.getStockQuantity());
    }

    // Маппинг строки ResultSet → объект Product
    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setDiscount(rs.getBigDecimal("discount"));
        p.setImagePath(rs.getString("image_path"));
        p.setStockQuantity(rs.getInt("stock_quantity"));

        p.setCategoryId(rs.getInt("category_id"));
        p.setCategoryName(rs.getString("category_name"));
        p.setManufacturerId(rs.getInt("manufacturer_id"));
        p.setManufacturerName(rs.getString("manufacturer_name"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setSupplierName(rs.getString("supplier_name"));
        p.setUnitId(rs.getInt("unit_id"));
        p.setUnitName(rs.getString("unit_name"));

        return p;
    }
}
