package com.example.smarthomeui.smarthome.ai_speech_reg;

public class Command {
    public enum Intent { ON, OFF, TOGGLE, SET_BRIGHTNESS, SET_SPEED, SET_TEMP, SET_COLOR }
    public String device; // "light", "fan", "ac"...
    public String location; // "living_room", "bedroom"...
    public Intent intent;
    public Integer percentOrValue; // 0..100 hoặc nhiệt độ
    public String color; // "red", "blue"...


    @Override
    public String toString() {
        return "Command{" +
                "intent=" + intent +
                ", device='" + device + '\'' +
                ", location='" + location + '\'' +
                ", percentOrValue=" + percentOrValue +
                ", color='" + color + '\'' +
                '}';
    }
}