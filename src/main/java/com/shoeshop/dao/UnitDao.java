package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Unit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы units. */
public class UnitDao {

    public List<Unit> findAll() throws SQLException {
        List<Unit> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name FROM units ORDER BY name")) {

            while (rs.next()) {
                list.add(new Unit(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }
}
