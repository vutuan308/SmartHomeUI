package com.example.smarthomeui.smarthome.ai_speech_reg;

import com.example.smarthomeui.smarthome.model.Device;
import java.util.*;
import java.util.regex.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.StringUtilsVN.fold;

public class VietnameseCommandParser {

    private final DeviceRegistry registry;
    private static final int MIN_DEVICE_SCORE = 6; // <6 → xem như không liên quan

    public VietnameseCommandParser(DeviceRegistry registry) { this.registry = registry; }

    private static final Map<String,Integer> NUMBER_WORDS = new HashMap<>();
    static {
        String[][] pairs = {
                {"khong","0"},{"mot","1"},{"một","1"},{"nhat","1"},{"nhất","1"},
                {"hai","2"},{"ba","3"},{"bon","4"},{"bốn","4"},{"nam","5"},{"năm","5"},
                {"sau","6"},{"sáu","6"},{"bay","7"},{"bảy","7"},{"tam","8"},{"tám","8"},
                {"chin","9"},{"chín","9"},{"muoi","10"},{"mười","10"}
        };
        for (String[] p: pairs) NUMBER_WORDS.put(p[0], Integer.parseInt(p[1]));
    }

    public ParseResult parse(String raw) {
        ParseResult out = new ParseResult();
        out.raw = raw;

        String f = fold(raw);
        if (f.isEmpty()) return out; // giữ UNKNOWN/-1

        // 1) Action
        if (f.matches(".*\\b(bat|mo|bật|mở|turn on)\\b.*")) out.action = Action.TURN_ON;
        else if (f.matches(".*\\b(tat|dong|tắt|đóng|turn off)\\b.*")) out.action = Action.TURN_OFF;
        else if (f.matches(".*\\b(tang|tăng|len|lên|up|increase)\\b.*")) out.action = Action.INCREASE;
        else if (f.matches(".*\\b(giam|giảm|xuong|xuống|down|decrease)\\b.*")) out.action = Action.DECREASE;
        else if (f.matches(".*\\b(dat|đặt|set)\\b.*")) out.action = Action.SET;

        // 2) Value
        Integer value = extractValue(f);
        out.value = (value != null) ? value : -1;

        // 3) Room
        String room = extractRoom(f);
        out.room = (room != null) ? room : "UNKNOWN";

        // 4) Đánh giá liên quan thiết bị
        int topScore = registry.estimateTopScore(f, out.isRoomKnown()? out.room : null);
        if (topScore < MIN_DEVICE_SCORE) {
            // Không liên quan: giữ device UNKNOWN/-1
            return out;
        }

        // 5) Ứng viên tốt nhất
        List<DeviceRegistry.CandidateResult> cands =
                registry.rankCandidates(f, out.isRoomKnown()? out.room : null, 1);
        if (!cands.isEmpty()) {
            Device d = cands.get(0).device;
            out.deviceId = d.getId();
            out.deviceName = d.getName();
            if (!out.isRoomKnown()) out.room = d.getRoom();
        }
        return out;
    }

    private Integer extractValue(String f) {
        Matcher m1 = Pattern.compile("\\bmuc|cap|level\\s+(\\d+)\\b").matcher(f);
        if (m1.find()) return Integer.parseInt(m1.group(1));
        Matcher m2 = Pattern.compile("\\b(\\d{1,3})\\s*%").matcher(f);
        if (m2.find()) return Integer.parseInt(m2.group(1));
        Matcher m3 = Pattern.compile("\\b(\\d{1,2})\\b").matcher(f);
        if (m3.find()) return Integer.parseInt(m3.group(1));
        for (Map.Entry<String,Integer> e : NUMBER_WORDS.entrySet())
            if (f.contains(e.getKey())) return e.getValue();
        return null;
    }

    private String extractRoom(String f) {
        Matcher mr = Pattern.compile("\\bphong|room\\s+(ngu|bedroom|khach|livingroom" +
                "|bep|kitchen|lam viec|hoc|tam|bathroom|wc)\\b").matcher(f);
        if (mr.find()) {
            switch (mr.group(1)) {
                case "ngu": return "phòng ngủ";
                case "khach": return "phòng khách";
                case "bep": return "phòng bếp";
                case "lam viec": return "phòng làm việc";
                case "hoc": return "phòng học";
                case "tam": return "phòng tắm";
                case "wc": return "phòng vệ sinh";
            }
        }
        return null;
    }
}