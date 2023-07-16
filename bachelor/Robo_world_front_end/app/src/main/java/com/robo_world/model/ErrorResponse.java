package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

/**
 * Error response dto
 *
 * @author Blajan George
 */
public class ErrorResponse {
    @SerializedName("error")
    ErrorInformation error;

    public ErrorResponse(ErrorInformation error) {
        this.error = error;
    }

    public ErrorInformation getError() {
        return error;
    }

    public void setError(ErrorInformation error) {
        this.error = error;
    }
}
