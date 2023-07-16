package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

public class GetUserResponse {
    @SerializedName("id")
    private Integer id;
    @SerializedName("role")
    private String role;
    @SerializedName("email")
    private String email;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("bots_number")
    private Integer botsNumber;
    @SerializedName("friends_number")
    private Integer friendsNumber;

    public GetUserResponse(Integer id, String role, String email, String firstName, String lastName, Integer botsNumber, Integer friendsNumber) {
        this.id = id;
        this.role = role;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.botsNumber = botsNumber;
        this.friendsNumber = friendsNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public Integer getBotsNumber() {
        return botsNumber;
    }

    public void setBotsNumber(Integer botsNumber) {
        this.botsNumber = botsNumber;
    }

    public Integer getFriendsNumber() {
        return friendsNumber;
    }

    public void setFriendsNumber(Integer friendsNumber) {
        this.friendsNumber = friendsNumber;
    }
}
