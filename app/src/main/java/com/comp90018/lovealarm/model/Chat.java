package com.comp90018.lovealarm.model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private String date;
    private String type;

    public Chat(String sender, String receiver, String message, String date, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.date = date;
        this.type = type;
    }

    public Chat() {
    }

    public String getSender() {
        return sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
