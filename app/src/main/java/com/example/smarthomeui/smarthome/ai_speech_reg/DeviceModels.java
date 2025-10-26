package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.io.Serializable;

public class DeviceModels {

    public enum Action implements Serializable {
        TURN_ON, TURN_OFF, INCREASE, DECREASE, SET, UNKNOWN
    }

    /** Kết quả NLU (giữ UNKNOWN/-1 nếu mơ hồ) */
    public static class ParseResult implements Serializable {
        public Action  action    = Action.UNKNOWN;
        public String deviceId  = "UNKNOWN";           // -1 khi không rõ
        public String  deviceName= "UNKNOWN";
        public String  room      = "UNKNOWN";
        public Integer value     = -1;           // -1 khi không rõ
        public String  raw;
        public int deviceOrdinal;

        public boolean isDeviceKnown() { return deviceId != null && !"UNKNOWN".equals(deviceId); }
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
}