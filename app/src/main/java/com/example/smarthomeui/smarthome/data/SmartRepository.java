package com.example.smarthomeui.smarthome.data;

import android.content.Context;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.model.House;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.*;

public class SmartRepository {
    private static SmartRepository instance;
    private final Map<String, House> houses = new LinkedHashMap<>();

    private SmartRepository(Context ctx) { seed(); }

    public static synchronized SmartRepository get(Context ctx) {
        if (instance == null) instance = new SmartRepository(ctx.getApplicationContext());
        return instance;
    }

    private void seed() {
        // House A
        House houseA = new House(UUID.randomUUID().toString(), "Nhà Quận 1", R.drawable.home);
        Room a_living = new Room(UUID.randomUUID().toString(), "Phòng khách", R.drawable.room);
        a_living.getDevices().add(new Device(UUID.randomUUID().toString(), "Đèn trần", "Light", true));
        a_living.getDevices().add(new Device(UUID.randomUUID().toString(), "Quạt đứng", "Fan", false));
        Room a_bed = new Room(UUID.randomUUID().toString(), "Phòng ngủ", R.drawable.room);
        a_bed.getDevices().add(new Device(UUID.randomUUID().toString(), "Đèn ngủ", "Light", false));
        houseA.getRooms().add(a_living);
        houseA.getRooms().add(a_bed);

        // House B
        House houseB = new House(UUID.randomUUID().toString(), "Nhà Biệt thự Q.7", R.drawable.home);
        Room b_kitchen = new Room(UUID.randomUUID().toString(), "Bếp", R.drawable.ic_room_kitchen);
        b_kitchen.getDevices().add(new Device(UUID.randomUUID().toString(), "Đèn bếp", "Light", true));
        Room b_office = new Room(UUID.randomUUID().toString(), "Phòng làm việc", R.drawable.ic_room_generic);
        b_office.getDevices().add(new Device(UUID.randomUUID().toString(), "Điều hòa", "AC", true));
        houseB.getRooms().add(b_kitchen);
        houseB.getRooms().add(b_office);

        houses.put(houseA.getId(), houseA);
        houses.put(houseB.getId(), houseB);
    }

    // ===== Houses =====
    public List<House> getHouses() { return new ArrayList<>(houses.values()); }
    public House getHouseById(String houseId) { return houses.get(houseId); }

    // ===== Rooms =====
    public List<Room> getRooms(String houseId) {
        House h = houses.get(houseId); return h == null ? Collections.emptyList() : h.getRooms();
    }
    public Room getRoomById(String houseId, String roomId) {
        House h = houses.get(houseId); if (h == null) return null;
        for (Room r : h.getRooms()) if (r.getId().equals(roomId)) return r;
        return null;
    }

    // ===== Devices =====
    public void addDevice(String houseId, String roomId, Device d) {
        Room r = getRoomById(houseId, roomId);
        if (r != null) r.getDevices().add(d);
    }
}