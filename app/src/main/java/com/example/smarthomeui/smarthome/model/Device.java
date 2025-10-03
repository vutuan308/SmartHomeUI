package com.example.smarthomeui.smarthome.model;

import java.io.Serializable;

/**
 * Device model class
 * Đại diện cho thiết bị IoT trong hệ thống smart home
 */
public class Device implements Serializable {
    private String id;
    private String name;
    private String room;
    private String type;
    private boolean isOnline;
    private int value; // Giá trị hiện tại (%, độ sáng, nhiệt độ...)
    private String powerConsumption;
    private String lastActivity;
    private String status;

    // Additional properties for compatibility with existing code
    private boolean isOn;
    private int brightness = 100;
    private int speed = 1;
    private int color = 0xFFFFFFFF; // Default white

    public Device() {}

    public Device(String id, String name, String room, String type, boolean isOnline, int value, String powerConsumption) {
        this.id = id;
        this.name = name;
        this.room = room;
        this.type = type;
        this.isOnline = isOnline;
        this.isOn = isOnline;
        this.value = value;
        this.powerConsumption = powerConsumption;
        this.lastActivity = "5 phút";
        this.status = isOnline ? "Online" : "Offline";
    }

    // Constructor for compatibility with existing code
    public Device(String id, String name, String type, boolean isOn) {
        this(id, name, "", type, isOn, isOn ? 100 : 0, "0W");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) {
        isOnline = online;
        isOn = online;
        this.status = online ? "Online" : "Offline";
    }

    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }

    public String getPowerConsumption() { return powerConsumption; }
    public void setPowerConsumption(String powerConsumption) { this.powerConsumption = powerConsumption; }

    public String getLastActivity() { return lastActivity; }
    public void setLastActivity(String lastActivity) { this.lastActivity = lastActivity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Compatibility methods for existing code
    public boolean isOn() { return isOn; }
    public void setOn(boolean on) {
        this.isOn = on;
        this.isOnline = on;
        this.status = on ? "Online" : "Offline";
    }

    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = brightness; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public boolean isLight() {
        return type != null && type.toLowerCase().contains("light");
    }

    public boolean isFan() {
        return type != null && type.toLowerCase().contains("fan");
    }

    public String getValueText() {
        return value + "%";
    }

    public int getStatusColor() {
        return isOnline ? android.R.color.holo_green_light : android.R.color.holo_red_light;
    }
}
