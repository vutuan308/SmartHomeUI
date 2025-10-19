package com.example.smarthomeui.smarthome.ai_speech_reg;

import com.example.smarthomeui.smarthome.model.Device;
import java.util.*;
import static com.example.smarthomeui.smarthome.ai_speech_reg.StringUtilsVN.fold;

public class DeviceRegistry {

    public static class CandidateResult {
        public final Device device;
        public final int score;
        public CandidateResult(Device d, int s) { this.device = d; this.score = s; }
    }

    private final List<Device> devices = new ArrayList<>();

    public DeviceRegistry() {
        // Mẫu in-memory; dùng field public của Device.java (id, name, room, type)
        devices.add(new Device("1", "Đèn số 1", "Phong khach", "Light", true, 75, "10W"));
        devices.add(new Device("2", "Đèn số 2", "Phong ngủ", "Light", true, 75, "10W"));
        devices.add(new Device("3", "Quạt điện", "Phong bếp", "Fan", true, 75, "10W"));
        devices.add(new Device("4", "Đèn ABC", "Huy", "Light", true, 75, "10W"));
        devices.add(new Device("5", "Tôi là ai", "Phòng khác", "Light", true, 75, "10W"));
    }

    public List<Device> getAll() { return Collections.unmodifiableList(devices); }
    public void add(Device d) { devices.add(d); }
    public void removeById(String id) { devices.removeIf(d -> Objects.equals(d.getId(), id)); }
    public void update(Device d) {
        for (int i=0;i<devices.size();i++) if (Objects.equals(devices.get(i).getId(), d.getId())) { devices.set(i, d); return; }
    }

    public List<CandidateResult> rankCandidates(String deviceText, String roomTextOpt, int limit) {
        String key = fold(deviceText + " " + (roomTextOpt == null ? "" : roomTextOpt));
        List<CandidateResult> list = new ArrayList<>();
        for (Device d : devices) {
            int score = 0;
            String nameF = fold(d.getName());
            String roomF = fold(d.getRoom());

            if (key.contains(nameF)) score += nameF.length();
            for (String token : nameF.split(" ")) if (key.contains(token)) score++;
            if (roomTextOpt != null && key.contains(roomF)) score += roomF.length();

            // Bonus nhẹ: tận dụng type/capabilities thông qua isLight()/isFan()
            if (key.contains("den")  && (d.isLight() || (d.getType() !=null && d.getType().contains("light")))) score += 2;
            if (key.contains("quat") && (d.isFan()   || (d.getType() !=null && d.getType().contains("fan"))))   score += 2;

            list.add(new CandidateResult(d, score));
        }
        list.sort((a, b) -> Integer.compare(b.score, a.score));
        return list.size() > limit ? list.subList(0, limit) : list;
    }

    public int estimateTopScore(String deviceText, String roomTextOpt) {
        List<CandidateResult> c = rankCandidates(deviceText, roomTextOpt, 1);
        return c.isEmpty() ? 0 : c.get(0).score;
    }
}