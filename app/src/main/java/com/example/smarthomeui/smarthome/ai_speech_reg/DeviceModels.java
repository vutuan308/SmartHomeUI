package com.example.smarthomeui.smarthome.ai_speech_reg;

public class DeviceModels {
    public enum Action {
        TURN_ON, TURN_OFF, INCREASE, DECREASE, SET, UNKNOWN
    }

    public static class ParseResult {
        public Action action = Action.UNKNOWN;
        public Integer deviceId = -1;        // -1 khi không rõ
        public String deviceName = "UNKNOWN";
        public String room = "UNKNOWN";
        public Integer value = -1;           // -1 khi không rõ
        public String raw;

        public boolean isDeviceKnown() { return deviceId != null && deviceId >= 0; }
        public boolean isActionKnown() { return action != Action.UNKNOWN; }
        public boolean isValueKnown()  { return value != null && value >= 0; }
        public boolean isRoomKnown()   { return room != null && !"UNKNOWN".equals(room); }

        @Override public String toString() {
            return "action=" + action +
                    ", deviceId=" + deviceId +
                    ", deviceName=" + deviceName +
                    ", room=" + room +
                    ", value=" + value;
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
