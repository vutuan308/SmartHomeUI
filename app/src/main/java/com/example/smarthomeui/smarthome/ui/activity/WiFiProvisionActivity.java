package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.listeners.ResponseListener;
import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.provision.ProvisionSession;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WiFiProvisionActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private final List<WiFiAccessPoint> aps = new ArrayList<>();
    private ESPProvisionManager provisionManager;

    public static void start(AppCompatActivity activity) {
        activity.startActivity(new android.content.Intent(activity, WiFiProvisionActivity.class));
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_provision);
        provisionManager = ESPProvisionManager.getInstance(this);

        TextView tvBack = findViewById(R.id.tvBack);
        TextView tvCancel = findViewById(R.id.tvCancel);
        tvBack.setOnClickListener(v -> finish());
        tvCancel.setOnClickListener(v -> finish());

        ListView listView = findViewById(R.id.listWifi);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listView.setAdapter(adapter);

        Button btnScan = findViewById(R.id.btnScan);
        Button btnManual = findViewById(R.id.btnManual);
        btnScan.setOnClickListener(v -> scan());
        btnManual.setOnClickListener(v -> showManualDialog(null));

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < aps.size()) {
                WiFiAccessPoint ap = aps.get(position);
                String ssid;
                try { ssid = ap.getWifiName(); } catch (Exception e) { ssid = String.valueOf(ap); }
                showManualDialog(ssid);
            }
        });

        // Kiểm tra xem có ESP device từ Bluetooth không
        ESPDevice device = ProvisionSession.get().getEspDevice();
        if (device != null) {
            Toast.makeText(this, "Thiết bị ESP đã kết nối qua Bluetooth. Đang quét WiFi...", Toast.LENGTH_SHORT).show();
            scan();
        } else {
            Toast.makeText(this, "Không có thiết bị ESP trong session. Vui lòng quay lại và kết nối Bluetooth trước.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void scan() {
        ESPDevice dev = ProvisionSession.get().getEspDevice();
        if (dev == null) {
            Toast.makeText(this, "Không có thiết bị ESP trong session", Toast.LENGTH_SHORT).show();
            return;
        }

        adapter.clear();
        adapter.add("Đang quét mạng WiFi...");
        aps.clear();

        try {
            dev.scanNetworks(new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    runOnUiThread(() -> updateList(wifiList));
                }

                @Override
                public void onWiFiScanFailed(Exception e) {
                    runOnUiThread(() -> {
                        adapter.clear();
                        adapter.add("Quét WiFi thất bại: " + e.getMessage());
                        Toast.makeText(WiFiProvisionActivity.this, "Quét WiFi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });
        } catch (SecurityException se) {
            adapter.clear();
            adapter.add("Thiếu quyền để quét WiFi");
            Toast.makeText(this, "Thiếu quyền để quét WiFi: " + se.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateList(ArrayList<WiFiAccessPoint> wifiList) {
        adapter.clear();
        aps.clear();
        if (wifiList != null) aps.addAll(wifiList);
        if (aps.isEmpty()) {
            adapter.add(getString(R.string.wifi_scan_failed, "No networks"));
        } else {
            for (WiFiAccessPoint ap : aps) {
                try { adapter.add(ap.getWifiName()); } catch (Exception e) { adapter.add(String.valueOf(ap)); }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void showManualDialog(@Nullable String prefillSsid) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_provision_wifi, null, false);
        EditText edtSsid = view.findViewById(R.id.edtSsid);
        EditText edtPass = view.findViewById(R.id.edtPass);
        if (prefillSsid != null) edtSsid.setText(prefillSsid);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_Material3_Dialog)
                .setTitle(R.string.title_connect_wifi)
                .setView(view)
                .setPositiveButton(R.string.connect, null)
                .setNegativeButton(R.string.cancel, (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String ssid = String.valueOf(edtSsid.getText()).trim();
                String pass = String.valueOf(edtPass.getText());
                if (ssid.isEmpty()) { edtSsid.setError(getString(R.string.demo_title)); return; }
                doProvision(ssid, pass, dialog);
            });
        });
        dialog.show();
    }

    private void doProvision(String ssid, String pass, @Nullable AlertDialog dismissOnSuccess) {
        ESPDevice dev = ProvisionSession.get().getEspDevice();
        if (dev == null) {
            Toast.makeText(this, R.string.no_device_in_session, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            dev.provision(ssid, pass, new ProvisionListener() {
                @Override public void createSessionFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.provision_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                }
                @Override public void wifiConfigSent() { }
                @Override public void wifiConfigFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.provision_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                }
                @Override public void wifiConfigApplied() { }
                @Override public void wifiConfigApplyFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.provision_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                }
                @Override public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.provision_failed, failureReason), Toast.LENGTH_SHORT).show());
                }
                @Override public void onProvisioningFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.provision_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                }
                @Override public void deviceProvisioningSuccess() {

                    var device = provisionManager.getEspDevice();
                    String json = "";
                    device.sendDataToCustomEndPoint("test", json.getBytes(), new ResponseListener() {
                                @Override
                                public void onSuccess(byte[] returnData) {
                                    Log.d("WiFiProvision", "Sent to device: " + new String(returnData));
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("WiFiProvision", "Failed to send to device: " + e.getMessage());
                                }
                            }
                    );

                    runOnUiThread(() -> {
                        Toast.makeText(WiFiProvisionActivity.this, "Provisioning thành công!", Toast.LENGTH_LONG).show();

                        // Add to inventory
                        String name = "ESP Device";
                        Device newDevice = new Device(UUID.randomUUID().toString(), name, "ESP Device", false);
                        newDevice.setToken(ssid);
                        newDevice.addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
                        newDevice.setBrightness(100);
                        newDevice.setColor(0xFFFFFFFF);


                        if (dismissOnSuccess != null) dismissOnSuccess.dismiss();

                        // Quay về DeviceInventoryActivity thay vì finish()
                        Intent intent = new Intent(WiFiProvisionActivity.this, DeviceInventoryActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    });
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, getString(R.string.provision_failed, se.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}
