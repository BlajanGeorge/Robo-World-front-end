package com.robo_world.model;

public class BotRequest {
    private String name;

    public BotRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
