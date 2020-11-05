package com.example.littleitalypizzeria.models;

public class Review {

    private String itemID, reviewBody, userID, name;

    public Review() {
    }

    public Review(String itemID, String reviewBody, String userID) {
        this.itemID = itemID;
        this.reviewBody = reviewBody;
        this.userID = userID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getReviewBody() {
        return reviewBody;
    }

    public void setReviewBody(String reviewBody) {
        this.reviewBody = reviewBody;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
