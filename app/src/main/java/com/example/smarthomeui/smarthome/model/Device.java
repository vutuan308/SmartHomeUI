package com.example.smarthomeui.smarthome.model;

import java.io.Serializable;

public class Device implements Serializable {
    private String id;
    private String name;
    private String type; // "Light" | "Fan" | "AC" | ...
    private boolean on;

    // ---- Thuộc tính mở rộng theo loại ----
    // Light
    private int brightness = 50;        // 0..100
    private int color = 0xFFFFFFFF;     // ARGB (mặc định trắng)

    // Fan
    private int speed = 1;              // 0..3

    // AC
    private int temperature = 24;       // 16..30 (tuỳ bạn)
    private String mode = "cool";       // "cool" | "dry" | "fan"...

    // ---- Constructors ----
    public Device(String id, String name, String type, boolean on) {
        this.id = id; this.name = name; this.type = type; this.on = on;
    }

    // Tuỳ chọn: constructor đầy đủ nếu cần khởi tạo nhanh
    public Device(String id, String name, String type, boolean on,
                  int brightness, int color, int speed, int temperature, String mode) {
        this(id, name, type, on);
        this.brightness = brightness;
        this.color = color;
        this.speed = speed;
        this.mode = mode;
    }

    // ---- Helpers nhận dạng loại ----
    public boolean isLight() {
        String t = type == null ? "" : type.toLowerCase();
        return t.contains("light") || t.contains("lamp") || t.contains("đèn") || t.contains("den");
    }
    public boolean isFan() {
        String t = type == null ? "" : type.toLowerCase();
        return t.contains("fan") || t.contains("quạt") || t.contains("quat");
    }


    // ---- Getters/Setters cơ bản ----
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isOn() { return on; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setOn(boolean on) { this.on = on; }

    // ---- Getters/Setters mở rộng ----
    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) { this.brightness = clamp(brightness, 0, 100); }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = clamp(speed, 0, 3); }



    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    // ---- utils ----
    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
