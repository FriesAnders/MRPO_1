package com.shoeshop.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Product {

    private int        id;
    private String     name;
    private int        categoryId;
    private String     categoryName;     // заполняется JOIN-ом
    private int        manufacturerId;
    private String     manufacturerName; // заполняется JOIN-ом
    private int        supplierId;
    private String     supplierName;     // заполняется JOIN-ом
    private int        unitId;
    private String     unitName;         // заполняется JOIN-ом
    private String     description;
    private BigDecimal price;
    private BigDecimal discount;         // процент скидки (0–100)
    private String     imagePath;
    private int        stockQuantity;

    public Product() {}

    // Цена с учётом скидки
    public BigDecimal getDiscountedPrice() {
        if (!hasDiscount()) {
            return price;
        }
        BigDecimal factor = BigDecimal.ONE.subtract(
                discount.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP));
        return price.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }

    // Есть ли ненулевая скидка
    public boolean hasDiscount() {
        return discount != null && discount.compareTo(BigDecimal.ZERO) > 0;
    }

    // Скидка превышает 15 % (особая подсветка строки)
    public boolean isHighDiscount() {
        return discount != null && discount.compareTo(BigDecimal.valueOf(15)) > 0;
    }

    // Товар отсутствует на складе
    public boolean isOutOfStock() {
        return stockQuantity <= 0;
    }

    // ── Геттеры ──────────────────────────────────────────────────────────────

    public int        getId()               { return id; }
    public String     getName()             { return name; }
    public int        getCategoryId()       { return categoryId; }
    public String     getCategoryName()     { return categoryName; }
    public int        getManufacturerId()   { return manufacturerId; }
    public String     getManufacturerName() { return manufacturerName; }
    public int        getSupplierId()       { return supplierId; }
    public String     getSupplierName()     { return supplierName; }
    public int        getUnitId()           { return unitId; }
    public String     getUnitName()         { return unitName; }
    public String     getDescription()      { return description; }
    public BigDecimal getPrice()            { return price; }
    public BigDecimal getDiscount()         { return discount; }
    public String     getImagePath()        { return imagePath; }
    public int        getStockQuantity()    { return stockQuantity; }

    // ── Сеттеры ──────────────────────────────────────────────────────────────

    public void setId(int id)                          { this.id               = id; }
    public void setName(String name)                   { this.name             = name; }
    public void setCategoryId(int id)                  { this.categoryId       = id; }
    public void setCategoryName(String name)           { this.categoryName     = name; }
    public void setManufacturerId(int id)              { this.manufacturerId   = id; }
    public void setManufacturerName(String name)       { this.manufacturerName = name; }
    public void setSupplierId(int id)                  { this.supplierId       = id; }
    public void setSupplierName(String name)           { this.supplierName     = name; }
    public void setUnitId(int id)                      { this.unitId           = id; }
    public void setUnitName(String name)               { this.unitName         = name; }
    public void setDescription(String description)     { this.description      = description; }
    public void setPrice(BigDecimal price)             { this.price            = price; }
    public void setDiscount(BigDecimal discount)       { this.discount         = discount; }
    public void setImagePath(String imagePath)         { this.imagePath        = imagePath; }
    public void setStockQuantity(int qty)              { this.stockQuantity    = qty; }
}
