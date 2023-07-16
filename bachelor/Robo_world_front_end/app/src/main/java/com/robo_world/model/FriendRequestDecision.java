package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

public class FriendRequestDecision {
    @SerializedName("id")
    private Integer id;
    @SerializedName("accepted")
    private boolean accepted;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isDecision() {
        return accepted;
    }

    public void setDecision(boolean decision) {
        this.accepted = decision;
    }

    public FriendRequestDecision(Integer id, boolean decision) {
        this.id = id;
        this.accepted = decision;
    }
}
