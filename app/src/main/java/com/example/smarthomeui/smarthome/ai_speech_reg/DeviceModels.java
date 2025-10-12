package com.example.smarthomeui.smarthome.ai_speech_reg;

public class DeviceModels {
    public enum Action {
        TURN_ON, TURN_OFF, INCREASE, DECREASE, SET, UNKNOWN
    }

    public static class ParseResult {
        public Action action = Action.UNKNOWN;
        public Integer deviceId;         // nullable
        public String deviceName;        // như người dùng đặt
        public String room;              // nullable
        public Integer value;            // mức/%, nullable
        public String raw;               // câu gốc

        @Override public String toString() {
            return "Đã " + action +
                    " cho thiết bị " + deviceId +
                    " tên " + deviceName +
                    " ở phòng " + room +
                    " với giá trị " + value;
        }
    }

    public static class Device {
        public int id;
        public String name;     // “Đèn số 1”, “Quạt đứng”, …
        public String room;     // “phòng ngủ”, “phòng khách”, …
        public String type;     // “light”, “fan”, “rgb_light”, …

        public Device(int id, String name, String room, String type) {
            this.id = id; this.name = name; this.room = room; this.type = type;
        }
    }
}
