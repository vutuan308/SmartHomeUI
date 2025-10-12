package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.util.*;
import java.util.regex.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.StringUtilsVN.fold;

public class VietnameseCommandParser {

    private final DeviceRegistry registry;

    public VietnameseCommandParser(DeviceRegistry registry) {
        this.registry = registry;
    }

    // map số viết chữ phổ biến -> int
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

        // 1) Action
        if (f.matches(".*\\b(bat|mo|bật|mở)\\b.*")) out.action = Action.TURN_ON;
        else if (f.matches(".*\\b(tat|dong|tắt|đóng)\\b.*")) out.action = Action.TURN_OFF;
        else if (f.matches(".*\\b(tang|tăng|len|lên)\\b.*")) out.action = Action.INCREASE;
        else if (f.matches(".*\\b(giam|giảm|xuong|xuống)\\b.*")) out.action = Action.DECREASE;
        else if (f.matches(".*\\b(dat|đặt|set)\\b.*")) out.action = Action.SET;
        else out.action = Action.UNKNOWN;

        // 2) Value (mức/%) — bắt “mức 3”, “về 70%”, “70 phần trăm”
        Integer value = extractValue(f);
        out.value = value;

        // 3) Room (tìm các cụm “phòng ...”)
        String room = extractRoom(f);
        out.room = room;

        // 4) Device: tìm cụm tên thiết bị tự do
        //    - Ưu tiên cụm sau từ khóa “đèn|quat|máy lạnh|cửa|rèm …” nếu có
        String devicePhrase = extractDevicePhrase(f);
        Device device = null;

        if (devicePhrase != null) {
            device = registry.findByPhrase(devicePhrase);
        }
        if (device == null) {
            // fuzzy theo toàn câu + room
            device = registry.fuzzyFind(f, room);
        }

        if (device != null) {
            out.deviceId = device.id;
            out.deviceName = device.name;
            if (out.room == null) out.room = device.room; // điền room từ registry nếu người dùng không nói
        }

        return out;
    }

    private Integer extractValue(String f) {
        // “mức 3”, “lên mức 2”, “về 70%”, “70 phan tram”, “tăng 1 mức”
        Pattern p1 = Pattern.compile("\\bmuc\\s+(\\d+)\\b");
        Matcher m1 = p1.matcher(f);
        if (m1.find()) return Integer.parseInt(m1.group(1));

        Pattern p2 = Pattern.compile("\\b(\\d{1,3})\\s*%"); // 0..100%
        Matcher m2 = p2.matcher(f);
        if (m2.find()) return Integer.parseInt(m2.group(1));

        Pattern p3 = Pattern.compile("\\b(\\d{1,2})\\b");   // số rời
        Matcher m3 = p3.matcher(f);
        if (m3.find()) return Integer.parseInt(m3.group(1));

        // từ số “một/hai/ba…”
        for (Map.Entry<String,Integer> e : NUMBER_WORDS.entrySet()) {
            if (f.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private String extractRoom(String f) {
        // đơn giản: tìm “phong …”
        Pattern pr = Pattern.compile("\\bphong\\s+(ngu|khach|bep|lam viec|hoc|tam|wc)\\b");
        Matcher mr = pr.matcher(f);
        if (mr.find()) {
            String token = mr.group(1);
            switch (token) {
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

    private String extractDevicePhrase(String f) {
        // Nhận diện nhanh vài loại phổ biến; có thể mở rộng danh sách từ khoá
        String[] heads = {"den", "quat", "may lanh", "rem", "cua", "o cam", "tivi"};
        for (String h : heads) {
            int idx = f.indexOf(h);
            if (idx >= 0) {
                // lấy chuỗi từ h đến hết/đến “phong …”
                String tail = f.substring(idx);
                int cut = tail.indexOf("phong ");
                String phrase = (cut > 0 ? tail.substring(0, cut) : tail);
                return phrase.trim();
            }
        }
        return null;
    }
}