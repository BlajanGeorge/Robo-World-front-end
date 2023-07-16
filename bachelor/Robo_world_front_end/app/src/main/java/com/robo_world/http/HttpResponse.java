package com.robo_world.http;

/**
 * Wrapper on {@link okhttp3.Response}
 *
 * @author Blajan George
 */
public class HttpResponse {
    private boolean successful;
    private String body;
    private int status;

    public HttpResponse(boolean successful, String body, int status) {
        this.successful = successful;
        this.body = body;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
