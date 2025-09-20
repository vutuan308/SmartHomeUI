package com.example.smarthomeui.smarthome.data;

import android.content.Context;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.*;

public class RoomRepository {
    private static RoomRepository instance;
    private final Map<String, Room> rooms = new LinkedHashMap<>();

    private RoomRepository(Context ctx) { seed(); }

    public static synchronized RoomRepository get(Context ctx) {
        if (instance == null) instance = new RoomRepository(ctx.getApplicationContext());
        return instance;
    }

    private void seed() {
        Room living = new Room(UUID.randomUUID().toString(), "Phòng khách", R.drawable.ic_room_living);
        living.getDevices().add(new Device(UUID.randomUUID().toString(),"Đèn trần","Light", true));
        living.getDevices().add(new Device(UUID.randomUUID().toString(),"Quạt đứng","Fan", false));

        Room bed = new Room(UUID.randomUUID().toString(), "Phòng ngủ", R.drawable.ic_room_bed);
        bed.getDevices().add(new Device(UUID.randomUUID().toString(),"Đèn ngủ","Light", false));

        Room kitchen = new Room(UUID.randomUUID().toString(), "Bếp", R.drawable.ic_room_kitchen);
        kitchen.getDevices().add(new Device(UUID.randomUUID().toString(),"Đèn bếp","Light", true));

        rooms.put(living.getId(), living);
        rooms.put(bed.getId(), bed);
        rooms.put(kitchen.getId(), kitchen);
    }

    public List<Room> getRooms() { return new ArrayList<>(rooms.values()); }
    public Room getRoomById(String id) { return rooms.get(id); }

    public void addDevice(String roomId, Device device) {
        Room r = rooms.get(roomId);
        if (r != null) r.getDevices().add(device);
    }
}
