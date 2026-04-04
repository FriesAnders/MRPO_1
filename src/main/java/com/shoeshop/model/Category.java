package com.shoeshop.model;

public class Category {

    private int    id;
    private String name;

    public Category() {}

    public Category(int id, String name) {
        this.id   = id;
        this.name = name;
    }

    public int    getId()   { return id; }
    public String getName() { return name; }

    public void setId(int id)        { this.id   = id; }
    public void setName(String name) { this.name = name; }

    // toString используется ComboBox для отображения значения
    @Override
    public String toString() { return name; }
}
