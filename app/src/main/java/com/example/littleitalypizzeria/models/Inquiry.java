package com.example.littleitalypizzeria.models;

public class Inquiry {

    private String body, response, userID;

    public Inquiry() {
    }

    public Inquiry(String body, String userID) {
        this.body = body;

        //Default response
        this.response = "Thank you for your inquiry. We will get back to you soon!";
        this.userID = userID;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
