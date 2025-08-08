package com.demo.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageResponse {
    @JsonProperty("response")
    private String response;

    @JsonProperty("serverId")
    private String serverId;

    @JsonProperty("receivedAt")
    private String receivedAt;

    public MessageResponse() {}

    public MessageResponse(String response, String serverId, String receivedAt) {
        this.response = response;
        this.serverId = serverId;
        this.receivedAt = receivedAt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }
}
