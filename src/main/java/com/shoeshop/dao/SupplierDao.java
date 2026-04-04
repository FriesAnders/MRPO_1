package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Supplier;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы suppliers. */
public class SupplierDao {

    public List<Supplier> findAll() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name FROM suppliers ORDER BY name")) {

            while (rs.next()) {
                list.add(new Supplier(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }
}
