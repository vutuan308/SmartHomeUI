package com.example.smarthomeui.smarthome.devices.model;

import android.graphics.Color;


public class Device {
    public enum Type { LIGHT, LIGHT_RGB, FAN }


    private final String id;
    private String name;
    private Type type;
    private String deviceToken;


    private boolean on;
    private int brightness; // LIGHT & LIGHT_RGB: 0..100
    private int fanLevel; // FAN: 1..3
    private int colorArgb; // LIGHT_RGB


    public Device(String id, String name, Type type, String deviceToken) {
        this.id = id; this.name = name; this.type = type; this.deviceToken = deviceToken;
        this.on = false; this.brightness = 50; this.fanLevel = 1; this.colorArgb = Color.WHITE;
    }


    public String getId() { return id; }
    public String getName() { return name; }
    public Type getType() { return type; }
    public String getDeviceToken() { return deviceToken; }
    public boolean isOn() { return on; }
    public int getBrightness() { return brightness; }
    public int getFanLevel() { return fanLevel; }
    public int getColorArgb() { return colorArgb; }


    public void setName(String name) { this.name = name; }
    public void setType(Type type) { this.type = type; }
    public void setDeviceToken(String token) { this.deviceToken = token; }
    public void setOn(boolean on) { this.on = on; }
    public void setBrightness(int brightness) { this.brightness = Math.max(0, Math.min(100, brightness)); }
    public void setFanLevel(int fanLevel) { this.fanLevel = Math.max(1, Math.min(3, fanLevel)); }
    public void setColorArgb(int argb) { this.colorArgb = argb; }
}