package com.example.smarthomeui.smarthome.ai_speech_reg;

public class ControlRequest {
    public String command; // "turn_on", "turn_off", "set_brightness"...
    public Integer value; // tuỳ chọn: độ sáng, tốc độ...


    public ControlRequest(String command) {
        this.command = command;
    }


    public ControlRequest(String command, Integer value) {
        this.command = command;
        this.value = value;
    }
}

