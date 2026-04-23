package com.shoeshop.service;

import com.shoeshop.dao.CategoryDao;
import com.shoeshop.dao.ManufacturerDao;
import com.shoeshop.dao.ProductDao;
import com.shoeshop.dao.SupplierDao;
import com.shoeshop.dao.UnitDao;
import com.shoeshop.model.Category;
import com.shoeshop.model.Manufacturer;
import com.shoeshop.model.Product;
import com.shoeshop.model.Supplier;
import com.shoeshop.model.Unit;

import java.sql.SQLException;
import java.util.List;

/**
 * Сервис для работы с товарами.
 * Инкапсулирует бизнес-логику: делегирует CRUD в ProductDao,
 * предоставляет справочники для форм.
 */
public class ProductService {

    private final ProductDao      productDao      = new ProductDao();
    private final CategoryDao     categoryDao     = new CategoryDao();
    private final ManufacturerDao manufacturerDao = new ManufacturerDao();
    private final SupplierDao     supplierDao     = new SupplierDao();
    private final UnitDao         unitDao         = new UnitDao();

    public List<Product>      findAll()             throws SQLException { return productDao.findAll(); }
    public List<Category>     findAllCategories()   throws SQLException { return categoryDao.findAll(); }
    public List<Manufacturer> findAllManufacturers() throws SQLException { return manufacturerDao.findAll(); }
    public List<Supplier>     findAllSuppliers()    throws SQLException { return supplierDao.findAll(); }
    public List<Unit>         findAllUnits()        throws SQLException { return unitDao.findAll(); }

    public int  getNextProductId()            throws SQLException { return productDao.getNextId(); }
    public boolean isProductUsedInOrders(int id) throws SQLException { return productDao.isUsedInOrders(id); }

    /** Сохраняет новый товар. */
    public void save(Product product) throws SQLException {
        productDao.insert(product);
    }

    /** Обновляет существующий товар. */
    public void update(Product product) throws SQLException {
        productDao.update(product);
    }

    /**
     * Удаляет товар.
     *
     * @throws IllegalStateException если товар присутствует в заказах
     */
    public void delete(int id) throws SQLException {
        if (productDao.isUsedInOrders(id)) {
            throw new IllegalStateException(
                    "Удаление невозможно: товар входит в один или несколько заказов.\n" +
                    "Сначала удалите соответствующие заказы или их позиции.");
        }
        productDao.delete(id);
    }
}
