package com.example.littleitalypizzeria.models;

import java.io.Serializable;

public class CartItem implements Serializable {

    private String itemID, name;
    private double unitPrice;
    private int quantity;

    public CartItem() {}

    public CartItem(String itemID, String name, double unitPrice, int quantity) {
        this.itemID = itemID;
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
