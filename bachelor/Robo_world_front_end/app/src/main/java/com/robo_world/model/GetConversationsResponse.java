package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetConversationsResponse {
    @SerializedName("user")
    private GetUserResponse userResponse;
    @SerializedName("messages")
    private List<Message> messages;

    public GetUserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(GetUserResponse userResponse) {
        this.userResponse = userResponse;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public GetConversationsResponse(GetUserResponse userResponse, List<Message> messages) {
        this.userResponse = userResponse;
        this.messages = messages;
    }
}
