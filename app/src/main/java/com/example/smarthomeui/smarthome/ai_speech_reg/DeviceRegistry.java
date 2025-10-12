package com.example.smarthomeui.smarthome.ai_speech_reg;

import java.util.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.Device;
import static com.example.smarthomeui.smarthome.ai_speech_reg.StringUtilsVN.fold;

import androidx.annotation.Nullable;

public class DeviceRegistry {
    private final List<Device> devices = new ArrayList<>();

    public DeviceRegistry() {
        // Thiết bị mẫu (tuỳ bạn đổi tên để mô phỏng người dùng đặt tuỳ thích)
        devices.add(new Device(1, "Đèn số 1", "phòng ngủ", "light"));
        devices.add(new Device(2, "Đèn số 2", "phòng khách", "light"));
        devices.add(new Device(3, "Quạt trần", "phòng khách", "fan"));
        devices.add(new Device(4, "Quạt đứng", "phòng ngủ", "fan"));
        devices.add(new Device(5, "Huy 123", "phòng bếp", "light"));
        devices.add(new Device(6, "Đèn ABC", "phòng bếp", "light"));
        devices.add(new Device(7, "Tuấn", "phòng bếp", "light"));
    }

    public List<Device> getAll() { return Collections.unmodifiableList(devices); }

    public void add(Device d) { devices.add(d); }
    public void removeById(int id) { devices.removeIf(d -> d.id == id); }
    public void update(Device d) {
        for (int i=0;i<devices.size();i++) if (devices.get(i).id == d.id) { devices.set(i, d); return; }
    }

    /** Tìm theo tên/room tự do (không dấu), ưu tiên khớp nhiều từ khóa. */
    public Device fuzzyFind(String deviceText, String roomTextOpt) {
        String key = fold(deviceText + " " + (roomTextOpt == null ? "" : roomTextOpt));
        int bestScore = -1; Device best = null;
        for (Device d : devices) {
            int score = 0;
            String nameF = fold(d.name);
            String roomF = fold(d.room);
            if (key.contains(nameF)) score += nameF.length();
            // cho phép nói thiếu từ, chấm điểm theo số token trùng
            for (String token : nameF.split(" ")) if (key.contains(token)) score++;
            if (roomTextOpt != null && key.contains(roomF)) score += roomF.length();
            if (score > bestScore) { bestScore = score; best = d; }
        }
        return best;
    }

    /** Tìm thiết bị theo cụm “đèn số 1”, “quạt trần”, “đèn bếp”… */
    public Device findByPhrase(String phrase) {
        String p = fold(phrase);
        for (Device d : devices) if (p.contains(fold(d.name))) return d;
        return null;
    }

    public Device findById(Integer id) {
        if (id == null) return null;
        for (Device d : devices) if (d.id == id) return d;
        return null;
    }

    // DeviceRegistry.java (thêm lớp và hàm mới)
    public static class CandidateResult {
        public final Device device;
        public final int score;
        public CandidateResult(Device d, int s) { this.device = d; this.score = s; }
    }

    public List<CandidateResult> rankCandidates(String deviceText, @Nullable String roomTextOpt, int limit) {
        String key = fold(deviceText + " " + (roomTextOpt == null ? "" : roomTextOpt));
        List<CandidateResult> list = new ArrayList<>();
        for (Device d : devices) {
            int score = 0;
            String nameF = fold(d.name);
            String roomF = fold(d.room);

            if (key.contains(nameF)) score += nameF.length();
            for (String token : nameF.split(" ")) if (key.contains(token)) score++;
            if (roomTextOpt != null && key.contains(roomF)) score += roomF.length();

            // Bonus nhỏ nếu type ăn khớp gợi ý từ khóa (tùy chọn)
            if (key.contains("den") && "light".equals(d.type)) score += 2;
            if (key.contains("quat") && "fan".equals(d.type)) score += 2;

            list.add(new CandidateResult(d, score));
        }
        list.sort((a, b) -> Integer.compare(b.score, a.score));
        if (list.size() > limit) return list.subList(0, limit);
        return list;
    }

}

