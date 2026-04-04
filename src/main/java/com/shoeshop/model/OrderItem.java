package com.shoeshop.model;

import java.math.BigDecimal;

public class OrderItem {

    private int        id;
    private int        orderId;
    private int        productId;
    private String     productName;   // заполняется JOIN-ом
    private int        quantity;
    private BigDecimal priceAtOrder;

    public OrderItem() {}

    public int        getId()           { return id; }
    public int        getOrderId()      { return orderId; }
    public int        getProductId()    { return productId; }
    public String     getProductName()  { return productName; }
    public int        getQuantity()     { return quantity; }
    public BigDecimal getPriceAtOrder() { return priceAtOrder; }

    public void setId(int id)                        { this.id           = id; }
    public void setOrderId(int orderId)              { this.orderId      = orderId; }
    public void setProductId(int productId)          { this.productId    = productId; }
    public void setProductName(String name)          { this.productName  = name; }
    public void setQuantity(int quantity)            { this.quantity     = quantity; }
    public void setPriceAtOrder(BigDecimal price)    { this.priceAtOrder = price; }
}
