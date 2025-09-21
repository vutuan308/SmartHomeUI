package com.example.smarthomeui.smarthome.data;

import android.content.Context;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.model.House;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.*;

/** Repo demo: Nhà → Phòng → Thiết bị (seed nhiều thiết bị để test UI) */
public class SmartRepository {
    private static SmartRepository instance;
    private final Map<String, House> houses = new LinkedHashMap<>();

    private SmartRepository(Context ctx) {
        seed(); // khởi tạo dữ liệu mẫu
    }

    public static synchronized SmartRepository get(Context ctx) {
        if (instance == null) instance = new SmartRepository(ctx.getApplicationContext());
        return instance;
    }

    /* ====================== SEED DATA (DEMO) ====================== */
    private void seed() {
        houses.clear();

        // -------- House A --------
        House houseA = new House(UUID.randomUUID().toString(), "Nhà Quận 1", R.drawable.home);

        Room a_living = new Room(UUID.randomUUID().toString(), "Phòng khách", R.drawable.ic_room_living);
        // Đèn trần (bật, 80%, màu ấm)
        a_living.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn trần", "Light", true,
                80, 0xFFF2C179, 0, 0, null
        ));
        // Đèn led hắt trần (tắt, 30%, xanh dương)
        a_living.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn led hắt", "Light", false,
                30, 0xFF4F8CFF, 0, 0, null
        ));
        // Quạt trần (bật, tốc độ 2)
        a_living.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Quạt trần", "Fan", true,
                0, 0, 2, 0, null
        ));


        Room a_bed = new Room(UUID.randomUUID().toString(), "Phòng ngủ", R.drawable.ic_room_bed);
        a_bed.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn ngủ", "Light", true,
                20, 0xFFF2C179, 0, 0, null
        ));
        a_bed.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Quạt hộp", "Fan", false,
                0, 0, 1, 0, null
        ));


        houseA.getRooms().add(a_living);
        houseA.getRooms().add(a_bed);

        // -------- House B --------
        House houseB = new House(UUID.randomUUID().toString(), "Biệt thự Q.7", R.drawable.home);

        Room b_kitchen = new Room(UUID.randomUUID().toString(), "Bếp", R.drawable.ic_room_kitchen);
        b_kitchen.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn bếp", "Light", true,
                70, 0xFFF2C179, 0, 0, null
        ));
        b_kitchen.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Quạt hút", "Fan", true,
                0, 0, 3, 0, null
        ));

        Room b_office = new Room(UUID.randomUUID().toString(), "Phòng làm việc", R.drawable.ic_room_generic);
        b_office.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn bàn", "Light", true,
                60, 0xFFFFFFFF, 0, 0, null
        ));

        b_office.getDevices().add(new Device(
                UUID.randomUUID().toString(), "Đèn RGB", "Light", true,
                75, 0xFF4F8CFF, 0, 0, null
        ));

        houseB.getRooms().add(b_kitchen);
        houseB.getRooms().add(b_office);

        houses.put(houseA.getId(), houseA);
        houses.put(houseB.getId(), houseB);
    }

    /** Reseed để test lại nhanh (xoá & tạo lại dữ liệu demo) */
    public void resetForDemo() {
        seed();
    }

    /* ====================== HOUSES ====================== */
    public List<House> getHouses() { return new ArrayList<>(houses.values()); }
    public House getHouseById(String houseId) { return houses.get(houseId); }
    /** Thêm một nhà mới đơn giản (icon mặc định) */
    public House addHouse(String name) {
        String id = UUID.randomUUID().toString();
        House h = new House(id, (name == null || name.trim().isEmpty()) ? "Nhà mới" : name.trim(), R.drawable.home);
        houses.put(id, h);
        return h;
    }
    public void updateHouse(String houseId, String name, String description) {
        House h = houses.get(houseId);
        if (h != null) {
            if (name != null && !name.trim().isEmpty()) h.setName(name.trim());
            h.setDescription(description);
        }
    }
    public boolean deleteHouse(String houseId) {
        return houses.remove(houseId) != null;
    }

    /* ====================== ROOMS ====================== */
    public List<Room> getRooms(String houseId) {
        House h = houses.get(houseId);
        return h == null ? Collections.emptyList() : h.getRooms();
    }
    public Room getRoomById(String houseId, String roomId) {
        House h = houses.get(houseId);
        if (h == null) return null;
        for (Room r : h.getRooms()) if (r.getId().equals(roomId)) return r;
        return null;
    }
    /** Tất cả phòng của mọi nhà (tiện cho AllRoomsActivity) */
    public List<Room> getAllRooms() {
        List<Room> res = new ArrayList<>();
        for (House h : houses.values()) res.addAll(h.getRooms());
        return res;
    }
    /** Thêm phòng mới cho 1 nhà */
    public Room addRoom(String houseId, String name, int iconRes) {
        House h = houses.get(houseId);
        if (h == null) return null;
        Room r = new Room(UUID.randomUUID().toString(),
                (name == null || name.trim().isEmpty()) ? "Phòng mới" : name.trim(),
                iconRes);
        h.getRooms().add(r);
        return r;
    }
    public void updateRoom(String houseId, String roomId, String name, String description) {
        Room r = getRoomById(houseId, roomId);
        if (r != null) {
            if (name != null && !name.trim().isEmpty()) r.setName(name.trim());
            r.setDescription(description);
        }
    }
    public boolean deleteRoom(String houseId, String roomId) {
        House h = houses.get(houseId);
        if (h == null) return false;
        Iterator<Room> it = h.getRooms().iterator();
        while (it.hasNext()) {
            Room r = it.next();
            if (r.getId().equals(roomId)) { it.remove(); return true; }
        }
        return false;
    }

    /* ====================== DEVICES ====================== */
    public void addDevice(String houseId, String roomId, Device d) {
        Room r = getRoomById(houseId, roomId);
        if (r != null) r.getDevices().add(d);
    }
    /** Lấy danh sách thiết bị của 1 phòng */
    public List<Device> getDevicesOfRoom(String houseId, String roomId) {
        Room r = getRoomById(houseId, roomId);
        return (r == null) ? Collections.emptyList() : r.getDevices();
    }
    /** Tất cả thiết bị của mọi nhà/phòng (tiện cho AllDevicesActivity) */
    public List<Device> getAllDevices() {
        List<Device> res = new ArrayList<>();
        for (House h : houses.values()) {
            for (Room r : h.getRooms()) res.addAll(r.getDevices());
        }
        return res;
    }
}
