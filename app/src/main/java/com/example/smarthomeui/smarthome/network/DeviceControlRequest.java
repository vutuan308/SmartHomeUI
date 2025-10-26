package com.example.smarthomeui.smarthome.network;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class DeviceControlRequest {
    @SerializedName("method")
    private String command;
    private JsonElement params; // có thể là object/số/bool/null

    public DeviceControlRequest() {}
    public DeviceControlRequest(String command) { this.command = command; }
    public DeviceControlRequest(String command, JsonElement params) {
        this.command = command; this.params = params;
    }

    public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }
    public JsonElement getParams() { return params; }
    public void setParams(JsonElement params) { this.params = params; }
}