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
    // (tuỳ chọn) dùng để tra houseId từ roomId
    private final Map<String, String> roomToHouse = new HashMap<>();

    // ==== KHO THIẾT BỊ (inventory) – độc lập với phòng ====
    private final List<Device> inventory = new ArrayList<>();

    private SmartRepository(Context ctx) { seed(); }

    public static synchronized SmartRepository get(Context ctx) {
        if (instance == null) instance = new SmartRepository(ctx.getApplicationContext());
        return instance;
    }

    /* ====================== INVENTORY (Kho) ====================== */
    public List<Device> getInventory() { return new ArrayList<>(inventory); }

    public void addToInventory(Device d) { inventory.add(d); }

    public void removeFromInventory(String deviceId) {
        for (Iterator<Device> it = inventory.iterator(); it.hasNext();) {
            if (it.next().getId().equals(deviceId)) { it.remove(); break; }
        }
    }

    /** Gán 1 thiết bị từ kho vào phòng (copy thuộc tính), có thể xoá khỏi kho sau khi gán */
    public void assignInventoryDeviceToRoom(String deviceId, String houseId, String roomId, boolean removeFromInventory) {
        Device src = null;
        for (Device d : inventory) if (d.getId().equals(deviceId)) { src = d; break; }
        if (src == null) return;

        Room r = getRoomById(houseId, roomId);
        if (r == null) return;

        // clone nông với ID mới trong phòng + copy token/capabilities/thuộc tính
        Device copy = new Device(UUID.randomUUID().toString(), src.getName(), src.getType(), src.isOn(), src.getToken());
        copy.addCaps(src.getCapabilities().toArray(new String[0]));
        copy.setBrightness(src.getBrightness());
        copy.setColor(src.getColor());
        copy.setSpeed(src.getSpeed());
        copy.setTemperature(src.getTemperature());

        r.getDevices().add(copy);
        if (removeFromInventory) removeFromInventory(deviceId);
    }

    /* ====================== SEED DATA (DEMO) ====================== */
    private void seed() {
        houses.clear();
        roomToHouse.clear();
        inventory.clear();

        // ---- Seed INVENTORY (kho) mẫu ----
        Device rgb = new Device(uuid(), "Bóng RGB E27", "light.rgb", false, "RGB-E27-001")
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        rgb.setBrightness(70);
        rgb.setColor(0xFFFFC107); // amber
        inventory.add(rgb);

        Device fan = new Device(uuid(), "Quạt trần 5 cánh", "fan.ceiling", false, "FAN-CEIL-002")
                .addCaps(Device.CAP_POWER, Device.CAP_SPEED);
        fan.setSpeed(2);
        inventory.add(fan);

        Device outlet = new Device(uuid(), "Ổ cắm Wi-Fi", "switch.outlet", true, "OUT-WIFI-003")
                .addCaps(Device.CAP_POWER);
        inventory.add(outlet);

        // ---- House A ----
        int icLiving  = safeIcon(R.drawable.ic_room_living, R.drawable.ic_room_generic);
        int icBed     = safeIcon(R.drawable.ic_room_bed, R.drawable.ic_room_generic);
        int icKitchen = safeIcon(R.drawable.ic_room_kitchen, R.drawable.ic_room_generic);

        House houseA = new House(uuid(), "Nhà Quận 1", R.drawable.home);

        Room a_living = new Room(uuid(), "Phòng khách", icLiving);
        // Đèn trần (bật, 80%, màu ấm)
        Device denTran = new Device(uuid(), "Đèn trần", "Light", true)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denTran.setBrightness(80);
        denTran.setColor(0xFFF2C179);
        a_living.getDevices().add(denTran);

        // Đèn led hắt trần (tắt, 30%, xanh dương)
        Device denHat = new Device(uuid(), "Đèn led hắt", "Light", false)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denHat.setBrightness(30);
        denHat.setColor(0xFF4F8CFF);
        a_living.getDevices().add(denHat);

        // Quạt trần (bật, tốc độ 2)
        Device quatTran = new Device(uuid(), "Quạt trần", "Fan", true)
                .addCaps(Device.CAP_POWER, Device.CAP_SPEED);
        quatTran.setSpeed(2);
        a_living.getDevices().add(quatTran);

        Room a_bed = new Room(uuid(), "Phòng ngủ", icBed);
        Device denNgu = new Device(uuid(), "Đèn ngủ", "Light", true)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denNgu.setBrightness(20);
        denNgu.setColor(0xFFF2C179);
        a_bed.getDevices().add(denNgu);

        Device quatHop = new Device(uuid(), "Quạt hộp", "Fan", false)
                .addCaps(Device.CAP_POWER, Device.CAP_SPEED);
        quatHop.setSpeed(1);
        a_bed.getDevices().add(quatHop);

        houseA.getRooms().add(a_living);
        houseA.getRooms().add(a_bed);

        // ---- House B ----
        House houseB = new House(uuid(), "Biệt thự Q.7", R.drawable.home);

        Room b_kitchen = new Room(uuid(), "Bếp", icKitchen);
        Device denBep = new Device(uuid(), "Đèn bếp", "Light", true)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denBep.setBrightness(70);
        denBep.setColor(0xFFF2C179);
        b_kitchen.getDevices().add(denBep);

        Device quatHut = new Device(uuid(), "Quạt hút", "Fan", true)
                .addCaps(Device.CAP_POWER, Device.CAP_SPEED);
        quatHut.setSpeed(3);
        b_kitchen.getDevices().add(quatHut);

        Room b_office = new Room(uuid(), "Phòng làm việc", safeIcon(R.drawable.ic_room_generic, R.drawable.ic_room_generic));
        Device denBan = new Device(uuid(), "Đèn bàn", "Light", true)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denBan.setBrightness(60);
        denBan.setColor(0xFFFFFFFF);
        b_office.getDevices().add(denBan);

        Device denRGB = new Device(uuid(), "Đèn RGB", "Light", true)
                .addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
        denRGB.setBrightness(75);
        denRGB.setColor(0xFF4F8CFF);
        b_office.getDevices().add(denRGB);

        houseB.getRooms().add(b_kitchen);
        houseB.getRooms().add(b_office);

        houses.put(houseA.getId(), houseA);
        houses.put(houseB.getId(), houseB);

        // fill roomToHouse
        for (House h : houses.values()) {
            for (Room r : h.getRooms()) roomToHouse.put(r.getId(), h.getId());
        }
    }

    private static String uuid(){ return UUID.randomUUID().toString(); }

    // Nếu icon A không tồn tại, fallback icon B để tránh crash
    private static int safeIcon(int preferred, int fallback) { return preferred == 0 ? fallback : preferred; }

    /** Reseed để test lại nhanh (xoá & tạo lại dữ liệu demo) */
    public void resetForDemo() { seed(); }

    /* ====================== HOUSES ====================== */
    public List<House> getHouses() { return new ArrayList<>(houses.values()); }
    public House getHouseById(String houseId) { return houses.get(houseId); }

    public House addHouse(String name) {
        String id = uuid();
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

    public boolean deleteHouse(String houseId) { return houses.remove(houseId) != null; }

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

    /** tất cả phòng của mọi nhà */
    public List<Room> getAllRooms() {
        List<Room> res = new ArrayList<>();
        for (House h : houses.values()) res.addAll(h.getRooms());
        return res;
    }

    public Room addRoom(String houseId, String name, int iconRes) {
        House h = houses.get(houseId);
        if (h == null) return null;
        Room r = new Room(uuid(),
                (name == null || name.trim().isEmpty()) ? "Phòng mới" : name.trim(),
                iconRes);
        h.getRooms().add(r);
        roomToHouse.put(r.getId(), houseId);
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
            if (r.getId().equals(roomId)) { it.remove(); roomToHouse.remove(roomId); return true; }
        }
        return false;
    }

    // tiện tra houseId theo roomId (dùng cho MainActivity -> RoomDetails)
    public String getHouseIdByRoomId(String roomId) { return roomToHouse.get(roomId); }

    /* ====================== DEVICES (trong phòng) ====================== */
    public void addDevice(String houseId, String roomId, Device d) {
        Room r = getRoomById(houseId, roomId);
        if (r != null) r.getDevices().add(d);
    }

    public List<Device> getDevicesOfRoom(String houseId, String roomId) {
        Room r = getRoomById(houseId, roomId);
        return (r == null) ? Collections.emptyList() : r.getDevices();
    }

    public List<Device> getAllDevices() {
        List<Device> res = new ArrayList<>();
        for (House h : houses.values()) {
            for (Room r : h.getRooms()) res.addAll(r.getDevices());
        }
        return res;
    }
}
