package com.example.littleitalypizzeria.models;

public class Item {

    private String itemID, name, type, description, imageURL;
    private double price;

    public Item() {}

    public Item(String name, String type, String description, double price, String imageURL) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.imageURL = imageURL;
    }

    public Item(String itemID, String name, String type, String description, String imageURL, double price) {
        this.itemID = itemID;
        this.name = name;
        this.type = type;
        this.description = description;
        this.imageURL = imageURL;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }
}
