package com.shoeshop.model;

import java.time.LocalDate;

public class Order {

    private int       id;
    private String    article;        // артикул заказа
    private Integer   userId;         // может быть null (анонимный заказ)
    private String    userName;       // заполняется JOIN-ом
    private int       statusId;
    private String    statusName;     // заполняется JOIN-ом
    private String    pickupAddress;
    private LocalDate orderDate;
    private LocalDate deliveryDate;   // может быть null

    public Order() {}

    // ── Геттеры ──────────────────────────────────────────────────────────────

    public int       getId()             { return id; }
    public String    getArticle()        { return article; }
    public Integer   getUserId()         { return userId; }
    public String    getUserName()       { return userName; }
    public int       getStatusId()       { return statusId; }
    public String    getStatusName()     { return statusName; }
    public String    getPickupAddress()  { return pickupAddress; }
    public LocalDate getOrderDate()      { return orderDate; }
    public LocalDate getDeliveryDate()   { return deliveryDate; }

    // ── Сеттеры ──────────────────────────────────────────────────────────────

    public void setId(int id)                          { this.id            = id; }
    public void setArticle(String article)             { this.article       = article; }
    public void setUserId(Integer userId)              { this.userId        = userId; }
    public void setUserName(String userName)           { this.userName      = userName; }
    public void setStatusId(int statusId)              { this.statusId      = statusId; }
    public void setStatusName(String statusName)       { this.statusName    = statusName; }
    public void setPickupAddress(String address)       { this.pickupAddress = address; }
    public void setOrderDate(LocalDate orderDate)      { this.orderDate     = orderDate; }
    public void setDeliveryDate(LocalDate date)        { this.deliveryDate  = date; }
}
