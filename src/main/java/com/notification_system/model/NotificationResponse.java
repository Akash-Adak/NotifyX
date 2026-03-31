package com.notification_system.model;

public class NotificationResponse {
    private String message;

    public NotificationResponse() {}

    public NotificationResponse(String message) {
        this.message = message;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
