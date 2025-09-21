package com.example.smarthomeui.smarthome.devices.data;

import android.graphics.Color;

import com.example.smarthomeui.smarthome.devices.model.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class DeviceRepository {
    private static DeviceRepository I;
    public static DeviceRepository get() { if (I == null) I = new DeviceRepository(); return I; }


    private final List<Device> items = new ArrayList<>();


    private DeviceRepository() {
// seed demo
        Device d1 = new Device(UUID.randomUUID().toString(), "Living Room Light", Device.Type.LIGHT, "token-light-1");
        d1.setOn(true); d1.setBrightness(70); items.add(d1);


        Device d2 = new Device(UUID.randomUUID().toString(), "Bedroom Fan", Device.Type.FAN, "token-fan-2");
        d2.setFanLevel(2); items.add(d2);


        Device d3 = new Device(UUID.randomUUID().toString(), "Desk RGB", Device.Type.LIGHT_RGB, "token-rgb-3");
        d3.setOn(true); d3.setBrightness(60); d3.setColorArgb(Color.rgb(255, 64, 129)); items.add(d3);
    }


    public List<Device> list() { return new ArrayList<>(items); }
    public Device find(String id) {
        for (Device d : items) if (d.getId().equals(id)) return d; return null;
    }


    public Device add(Device d) { items.add(d); return d; }
    public boolean delete(String id) { return items.removeIf(d -> d.getId().equals(id)); }
}
