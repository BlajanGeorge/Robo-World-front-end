package com.robo_world.model;

import com.google.gson.annotations.SerializedName;

/**
 * User bots model
 *
 * @author Blajan George
 */
public class GetBotsResponse {
    @SerializedName("id")
    private Integer id;
    @SerializedName("name")
    private String name;
    @SerializedName("selected")
    private boolean selected;

    public GetBotsResponse(Integer id, String name, String macAddress, boolean selected) {
        this.id = id;
        this.name = name;
        this.selected = selected;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
