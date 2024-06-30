package com.crio.qeats.models;


public class ErrorResponseEntity {
    private String message;

    public ErrorResponseEntity(String message) {
        this.message = message;
    }

    // Getter and setter for message (if necessary)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
