package com.example.smarthomeui.smarthome.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Room implements Serializable {
    private String id;
    private String name;
    private int iconRes;
    private String description; // mô tả tuỳ chọn
    private final List<Device> devices = new ArrayList<>();

    public Room(String id, String name, int iconRes) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
    public List<Device> getDevices() { return devices; }
    public int getDeviceCount() { return devices.size(); }

    public String getDescription() { return description; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
