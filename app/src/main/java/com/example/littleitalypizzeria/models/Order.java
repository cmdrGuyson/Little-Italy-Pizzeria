package com.example.littleitalypizzeria.models;

import java.util.Map;

public class Order {
    private OrderInfo info;
    private Map<String, Item> items;

    public Order() {}

    public OrderInfo getInfo() {
        return info;
    }

    public void setInfo(OrderInfo info) {
        this.info = info;
    }

    public Map<String, Item> getItems() {
        return items;
    }

    public void setItems(Map<String, Item> items) {
        this.items = items;
    }
}
