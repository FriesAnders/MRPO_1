package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы categories. */
public class CategoryDao {

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name FROM categories ORDER BY name")) {

            while (rs.next()) {
                list.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }
}
