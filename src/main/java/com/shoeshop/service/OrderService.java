package com.shoeshop.service;

import com.shoeshop.dao.OrderDao;
import com.shoeshop.dao.OrderStatusDao;
import com.shoeshop.model.Order;
import com.shoeshop.model.OrderStatus;

import java.sql.SQLException;
import java.util.List;

/**
 * Сервис для работы с заказами.
 */
public class OrderService {

    private final OrderDao       orderDao       = new OrderDao();
    private final OrderStatusDao orderStatusDao = new OrderStatusDao();

    public List<Order>       findAll()         throws SQLException { return orderDao.findAll(); }
    public List<OrderStatus> findAllStatuses() throws SQLException { return orderStatusDao.findAll(); }

    /**
     * Проверяет уникальность артикула перед сохранением/обновлением.
     *
     * @param article   артикул заказа
     * @param excludeId id редактируемого заказа (0 при создании нового)
     */
    public boolean isArticleExists(String article, int excludeId) throws SQLException {
        return orderDao.isArticleExists(article, excludeId);
    }

    /** Сохраняет новый заказ. */
    public void save(Order order) throws SQLException {
        orderDao.insert(order);
    }

    /** Обновляет существующий заказ. */
    public void update(Order order) throws SQLException {
        orderDao.update(order);
    }

    /** Удаляет заказ вместе со всеми его позициями (ON DELETE CASCADE). */
    public void delete(int id) throws SQLException {
        orderDao.delete(id);
    }
}
