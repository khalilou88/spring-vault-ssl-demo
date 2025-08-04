package com.demo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageRequest {
    @JsonProperty("message")
    private String message;

    @JsonProperty("timestamp")
    private String timestamp;

    public MessageRequest() {
    }

    public MessageRequest(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

