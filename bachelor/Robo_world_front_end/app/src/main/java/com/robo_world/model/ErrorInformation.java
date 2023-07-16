package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

/**
 * Error dto
 *
 * @author Blajan George
 */
public class ErrorInformation {
    @SerializedName("message")
    String message;
    @SerializedName("type")
    String type;

    public ErrorInformation(String message, String type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
