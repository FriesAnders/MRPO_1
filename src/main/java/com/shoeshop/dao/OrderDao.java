package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Order;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO для таблицы orders. */
public class OrderDao {

    private static final String SELECT_BASE = """
            SELECT o.id, o.article, o.user_id, o.status_id,
                   os.name AS status_name,
                   o.pickup_address, o.order_date, o.delivery_date,
                   u.last_name || ' ' || u.first_name AS user_name
            FROM orders o
            JOIN order_statuses os ON o.status_id = os.id
            LEFT JOIN users u ON o.user_id = u.id
            """;

    /** Возвращает все заказы, упорядоченные по дате заказа (новые первыми). */
    public List<Order> findAll() throws SQLException {
        List<Order> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     SELECT_BASE + "ORDER BY o.order_date DESC, o.id DESC")) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Находит заказ по id. */
    public Optional<Order> findById(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE o.id = ?")) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    /**
     * Проверяет уникальность артикула заказа.
     *
     * @param article артикул для проверки
     * @param excludeId id заказа, который исключается из проверки (при редактировании)
     */
    public boolean isArticleExists(String article, int excludeId) throws SQLException {
        String sql = "SELECT 1 FROM orders WHERE article = ? AND id <> ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, article);
            stmt.setInt(2, excludeId);
            return stmt.executeQuery().next();
        }
    }

    /** Добавляет новый заказ и проставляет сгенерированный id в объект. */
    public void insert(Order order) throws SQLException {
        String sql = """
                INSERT INTO orders
                    (article, user_id, status_id, pickup_address, order_date, delivery_date)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            fillStatement(stmt, order);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getInt(1));
            }
        }
    }

    /** Обновляет существующий заказ. */
    public void update(Order order) throws SQLException {
        String sql = """
                UPDATE orders SET
                    article        = ?,
                    user_id        = ?,
                    status_id      = ?,
                    pickup_address = ?,
                    order_date     = ?,
                    delivery_date  = ?
                WHERE id = ?
                """;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            fillStatement(stmt, order);
            stmt.setInt(7, order.getId());
            stmt.executeUpdate();
        }
    }

    /** Удаляет заказ и все его позиции (каскадно, см. ON DELETE CASCADE). */
    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM orders WHERE id = ?")) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Заполняет PreparedStatement полями заказа (без id)
    private void fillStatement(PreparedStatement stmt, Order o) throws SQLException {
        stmt.setString(1, o.getArticle());
        if (o.getUserId() != null) {
            stmt.setInt(2, o.getUserId());
        } else {
            stmt.setNull(2, Types.INTEGER);
        }
        stmt.setInt(3, o.getStatusId());
        stmt.setString(4, o.getPickupAddress());
        stmt.setDate(5, Date.valueOf(o.getOrderDate()));
        if (o.getDeliveryDate() != null) {
            stmt.setDate(6, Date.valueOf(o.getDeliveryDate()));
        } else {
            stmt.setNull(6, Types.DATE);
        }
    }

    // Маппинг строки ResultSet → объект Order
    private Order mapRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setArticle(rs.getString("article"));

        int userId = rs.getInt("user_id");
        o.setUserId(rs.wasNull() ? null : userId);

        o.setStatusId(rs.getInt("status_id"));
        o.setStatusName(rs.getString("status_name"));
        o.setPickupAddress(rs.getString("pickup_address"));

        Date orderDate = rs.getDate("order_date");
        o.setOrderDate(orderDate != null ? orderDate.toLocalDate() : null);

        Date deliveryDate = rs.getDate("delivery_date");
        o.setDeliveryDate(deliveryDate != null ? deliveryDate.toLocalDate() : null);

        o.setUserName(rs.getString("user_name"));
        return o;
    }
}
