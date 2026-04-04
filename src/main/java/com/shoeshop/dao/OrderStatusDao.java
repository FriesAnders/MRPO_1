package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.OrderStatus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы order_statuses. */
public class OrderStatusDao {

    public List<OrderStatus> findAll() throws SQLException {
        List<OrderStatus> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name FROM order_statuses ORDER BY id")) {

            while (rs.next()) {
                list.add(new OrderStatus(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }
}
