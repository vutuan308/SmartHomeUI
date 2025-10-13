package com.example.smarthomeui.smarthome.network;

public class DeviceDto {
    private int id;
    private String name;
    private String userId;
    private String type;

    public DeviceDto() {}

    public DeviceDto(int id, String name, String userId, String type) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.type = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
