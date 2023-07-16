package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

/**
 * Login response model
 *
 * @author Blajan George
 */
public class LoginResponse {
    @SerializedName("id")
    private Integer id;
    @SerializedName("role")
    private String role;
    @SerializedName("token")
    private String token;
    @SerializedName("refresh_token")
    private String refreshToken;
    @SerializedName("selected_bot_name")
    private String botName;

    public LoginResponse(Integer id, String role, String token, String refreshToken, String botName) {
        this.id = id;
        this.role = role;
        this.token = token;
        this.refreshToken = refreshToken;
        this.botName = botName;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getBotName() {
        return botName;
    }

    public void setBotName(String botName) {
        this.botName = botName;
    }
}
