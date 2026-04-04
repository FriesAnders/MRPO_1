package com.shoeshop.dao;

import com.shoeshop.config.DatabaseConfig;
import com.shoeshop.model.Manufacturer;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** DAO для таблицы manufacturers. */
public class ManufacturerDao {

    public List<Manufacturer> findAll() throws SQLException {
        List<Manufacturer> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT id, name FROM manufacturers ORDER BY name")) {

            while (rs.next()) {
                list.add(new Manufacturer(rs.getInt("id"), rs.getString("name")));
            }
        }
        return list;
    }
}
