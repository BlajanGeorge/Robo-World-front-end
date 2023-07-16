package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

public class GetFriendRequestResponse {
    @SerializedName("request_id")
    private Integer requestId;
    @SerializedName("requester_id")
    private Integer requesterId;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Integer requesterId) {
        this.requesterId = requesterId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public GetFriendRequestResponse(Integer requestId, Integer requesterId, String firstName, String lastName) {
        this.requestId = requestId;
        this.requesterId = requesterId;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
