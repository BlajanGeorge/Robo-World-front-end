package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("subject")
    private Integer subject;
    @SerializedName("destination")
    private Integer destination;
    @SerializedName("content")
    private String content;
    @SerializedName("timestamp")
    private String timestamp;

    public Integer getSubject() {
        return subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getDestination() {
        return destination;
    }

    public void setDestination(Integer destination) {
        this.destination = destination;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Message(Integer subject, Integer destination, String content, String timestamp) {
        this.subject = subject;
        this.destination = destination;
        this.content = content;
        this.timestamp = timestamp;
    }
}
