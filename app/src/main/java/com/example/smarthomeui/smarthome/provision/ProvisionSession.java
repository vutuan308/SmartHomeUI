package com.example.smarthomeui.smarthome.provision;

import androidx.annotation.Nullable;

import com.espressif.provisioning.ESPDevice;

public class ProvisionSession {
    private static ProvisionSession instance;
    @Nullable private ESPDevice espDevice;
    private String pop = "abcd1234";

    private ProvisionSession() {}

    public static synchronized ProvisionSession get() {
        if (instance == null) instance = new ProvisionSession();
        return instance;
    }

    public void setEspDevice(@Nullable ESPDevice device) { this.espDevice = device; }
    @Nullable public ESPDevice getEspDevice() { return espDevice; }

    public String getPop() { return pop; }
    public void setPop(String pop) { this.pop = pop; }

    public void clear() { espDevice = null; }
}

