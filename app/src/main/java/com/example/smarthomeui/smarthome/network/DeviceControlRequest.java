package com.example.smarthomeui.smarthome.network;

public class DeviceControlRequest {
    private String command;

    public DeviceControlRequest(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
