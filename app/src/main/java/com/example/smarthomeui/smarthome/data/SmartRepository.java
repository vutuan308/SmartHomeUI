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

    // Thêm inventory để lưu trữ thiết bị chưa được gán vào phòng
    private final List<Device> inventory = new ArrayList<>();

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
        Device device1 = new Device(UUID.randomUUID().toString(), "Đèn trần", "Phòng khách", "Light", true, 80, "12W");
        device1.setBrightness(80);
        device1.setColor(0xFFF2C179);
        a_living.getDevices().add(device1);

        // Đèn led hắt trần (tắt, 30%, xanh dương)
        Device device2 = new Device(UUID.randomUUID().toString(), "Đèn led hắt", "Phòng khách", "Light", false, 30, "8W");
        device2.setBrightness(30);
        device2.setColor(0xFF4F8CFF);
        a_living.getDevices().add(device2);

        // Quạt trần (bật, tốc độ 2)
        Device device3 = new Device(UUID.randomUUID().toString(), "Quạt trần", "Phòng khách", "Fan", true, 100, "75W");
        device3.setSpeed(2);
        a_living.getDevices().add(device3);

        Room a_bed = new Room(UUID.randomUUID().toString(), "Phòng ngủ", R.drawable.ic_room_bed);
        Device device4 = new Device(UUID.randomUUID().toString(), "Đèn ngủ", "Phòng ngủ", "Light", true, 20, "5W");
        device4.setBrightness(20);
        device4.setColor(0xFFF2C179);
        a_bed.getDevices().add(device4);

        Device device5 = new Device(UUID.randomUUID().toString(), "Quạt hộp", "Phòng ngủ", "Fan", false, 0, "45W");
        device5.setSpeed(1);
        a_bed.getDevices().add(device5);

        houseA.getRooms().add(a_living);
        houseA.getRooms().add(a_bed);

        // -------- House B --------
        House houseB = new House(UUID.randomUUID().toString(), "Biệt thự Q.7", R.drawable.home);

        Room b_kitchen = new Room(UUID.randomUUID().toString(), "Bếp", R.drawable.ic_room_kitchen);
        Device device6 = new Device(UUID.randomUUID().toString(), "Đèn bếp", "Bếp", "Light", true, 70, "15W");
        device6.setBrightness(70);
        device6.setColor(0xFFF2C179);
        b_kitchen.getDevices().add(device6);

        Device device7 = new Device(UUID.randomUUID().toString(), "Quạt hút", "Bếp", "Fan", true, 100, "30W");
        device7.setSpeed(3);
        b_kitchen.getDevices().add(device7);

        Room b_office = new Room(UUID.randomUUID().toString(), "Phòng làm việc", R.drawable.ic_room_generic);
        Device device8 = new Device(UUID.randomUUID().toString(), "Đèn bàn", "Phòng làm việc", "Light", true, 60, "10W");
        device8.setBrightness(60);
        device8.setColor(0xFFFFFFFF);
        b_office.getDevices().add(device8);

        Device device9 = new Device(UUID.randomUUID().toString(), "Đèn RGB", "Phòng làm việc", "Light", true, 75, "12W");
        device9.setBrightness(75);
        device9.setColor(0xFF4F8CFF);
        b_office.getDevices().add(device9);

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

    /* ====================== INVENTORY (THIẾT BỊ CHƯA GÁN PHÒNG) ====================== */

    /**
     * Lấy danh sách thiết bị trong inventory (chưa được gán vào phòng nào)
     * @return List các thiết bị trong inventory
     */
    public List<Device> getInventory() {
        return new ArrayList<>(inventory);
    }

    /**
     * Thêm thiết bị vào inventory
     * @param device Thiết bị cần thêm
     */
    public void addToInventory(Device device) {
        if (device != null) {
            inventory.add(device);
        }
    }

    /**
     * Xóa thiết bị khỏi inventory theo ID
     * @param deviceId ID của thiết bị cần xóa
     * @return true nếu xóa thành công, false nếu không tìm thấy
     */
    public boolean removeFromInventory(String deviceId) {
        if (deviceId == null) return false;

        Iterator<Device> iterator = inventory.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            if (deviceId.equals(device.getId())) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    /**
     * Gán thiết bị từ inventory vào một phòng cụ thể
     * @param deviceId ID thiết bị trong inventory
     * @param houseId ID nhà
     * @param roomId ID phòng
     * @return true nếu gán thành công
     */
    public boolean assignInventoryDeviceToRoom(String deviceId, String houseId, String roomId) {
        if (deviceId == null || houseId == null || roomId == null) return false;

        // Tìm thiết bị trong inventory
        Device deviceToAssign = null;
        Iterator<Device> iterator = inventory.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            if (deviceId.equals(device.getId())) {
                deviceToAssign = device;
                iterator.remove(); // Xóa khỏi inventory
                break;
            }
        }

        if (deviceToAssign == null) return false;

        // Thêm vào phòng
        Room room = getRoomById(houseId, roomId);
        if (room != null) {
            deviceToAssign.setRoom(room.getName()); // Cập nhật room name
            room.getDevices().add(deviceToAssign);
            return true;
        }

        // Nếu không tìm thấy phòng, thêm lại vào inventory
        inventory.add(deviceToAssign);
        return false;
    }
}
