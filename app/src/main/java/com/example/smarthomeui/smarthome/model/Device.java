package com.example.smarthomeui.smarthome.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Device implements Serializable {

    // ==== Capability keys (dùng dạng String cho đơn giản) ====
    public static final String CAP_POWER        = "power";
    public static final String CAP_BRIGHTNESS   = "brightness";
    public static final String CAP_COLOR        = "color";
    public static final String CAP_SPEED        = "speed";
    public static final String CAP_TEMPERATURE  = "temperature";

    private String id;
    private String name;
    private String type;     // "Light", "Fan", "AC", "Outlet", ...
    private boolean on;

    // Token để đăng ký thiết bị (yêu cầu của bạn)
    private String token;

    // Tập capabilities của thiết bị
    private final Set<String> capabilities = new LinkedHashSet<>();

    // Thuộc tính mở rộng (tuỳ capability)
    private int brightness = 100;     // 0..100
    private int color      = 0xFFFFFFFF; // ARGB
    private int speed      = 0;       // 0..3
    private int temperature = 25;     // ví dụ °C

    public Device(String id, String name, String type, boolean on) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.on = on;
    }

    public Device(String id, String name, String type, boolean on, String token) {
        this(id, name, type, on);
        this.token = token;
    }

    // ======= GET/SET cơ bản =======
    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isOn() { return on; }
    public String getToken() { return token; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setOn(boolean on) { this.on = on; }
    public void setToken(String token) { this.token = token; }

    // ======= Capabilities API (giải quyết lỗi .has / CAP_COLOR) =======
    public Device addCaps(String... caps) {
        if (caps != null) capabilities.addAll(Arrays.asList(caps));
        return this;
    }
    public boolean has(String cap) { return capabilities.contains(cap); }
    public Set<String> getCapabilities() { return Collections.unmodifiableSet(capabilities); }

    // ======= Thuộc tính mở rộng =======
    public int getBrightness() { return brightness; }
    public void setBrightness(int brightness) {
        this.brightness = Math.max(0, Math.min(100, brightness));
    }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) {
        this.speed = Math.max(0, Math.min(3, speed));
    }

    public int getTemperature() { return temperature; }
    public void setTemperature(int temperature) { this.temperature = temperature; }

    // ======= Helpers theo loại (nếu bạn vẫn dùng) =======
    public boolean isLight() {
        // Ưu tiên capabilities, fallback theo type
        return has(CAP_BRIGHTNESS) || has(CAP_COLOR) ||
                (type != null && type.toLowerCase().contains("light"));
    }
    public boolean isFan() {
        return has(CAP_SPEED) ||
                (type != null && type.toLowerCase().contains("fan"));
    }
    public boolean isAC() {
        return has(CAP_TEMPERATURE) ||
                (type != null && (type.equalsIgnoreCase("ac") || type.toLowerCase().contains("air")));
    }
}
