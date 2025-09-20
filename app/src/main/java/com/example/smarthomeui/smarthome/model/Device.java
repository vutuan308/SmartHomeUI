package com.example.smarthomeui.smarthome.model;


import java.io.Serializable;


public class Device implements Serializable {
    private String id;
    private String name;
    private String type; // e.g., Light, Fan, AC
    private boolean on;


    public Device(String id, String name, String type, boolean on) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.on = on;
    }


    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isOn() { return on; }


    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setOn(boolean on) { this.on = on; }
}