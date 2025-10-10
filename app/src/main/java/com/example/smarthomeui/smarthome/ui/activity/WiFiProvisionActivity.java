package com.example.smarthomeui.smarthome.ui.activity;

import android.os.Bundle;
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

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.provision.ProvisionSession;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.ProvisionListener;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.espressif.provisioning.ESPProvisionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class WiFiProvisionActivity extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private final List<WiFiAccessPoint> aps = new ArrayList<>();

    public static void start(AppCompatActivity activity) {
        activity.startActivity(new android.content.Intent(activity, WiFiProvisionActivity.class));
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_provision);

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

        // Nếu đã có thiết bị trong session -> quét mạng ngay, ngược lại -> tìm và kết nối SoftAP của ESP
        if (ProvisionSession.get().getEspDevice() != null) {
            scan();
        } else {
            discoverAndConnectSoftAp();
        }
    }

    private void discoverAndConnectSoftAp() {
        adapter.clear();
        adapter.add(getString(R.string.wifi_scanning));
        try {
            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);
            pm.searchWiFiEspDevices("", new WiFiScanListener() {
                @Override public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    runOnUiThread(() -> {
                        if (wifiList == null || wifiList.isEmpty()) {
                            adapter.clear();
                            adapter.add(getString(R.string.wifi_scan_failed, getString(R.string.no_device_in_session)));
                            Toast.makeText(WiFiProvisionActivity.this, "Không tìm thấy ESP SoftAP", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showSoftApPicker(wifiList);
                    });
                }
                @Override public void onWiFiScanFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, "Quét SoftAP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền để tìm SoftAP: " + se.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khi tìm SoftAP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSoftApPicker(ArrayList<WiFiAccessPoint> wifiList) {
        List<String> names = new ArrayList<>();
        for (WiFiAccessPoint ap : wifiList) {
            try { names.add(ap.getWifiName()); } catch (Exception e) { names.add(String.valueOf(ap)); }
        }
        String[] items = names.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Chọn thiết bị ESP (SoftAP)")
                .setItems(items, (d, which) -> connectSoftAp(items[which]))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void connectSoftAp(String deviceSsid) {
        try {
            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);
            ESPDevice dev = pm.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP,
                                               ESPConstants.SecurityType.SECURITY_2);
            // Set PoP nếu có
            try {
                String pop = ProvisionSession.get().getPop();
                if (pop != null && !pop.isEmpty()) dev.setProofOfPossession(pop);
            } catch (Exception ignored) {}

            // Kết nối tới SoftAP
            dev.connectToDevice();
            // Lưu vào session
            ProvisionSession.get().setEspDevice(dev);
            Toast.makeText(this, "Đã kết nối thiết bị. Đang quét WiFi...", Toast.LENGTH_SHORT).show();
            // Quét WiFi quanh thiết bị ESP
            scan();
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền mạng để kết nối SoftAP: " + se.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Kết nối SoftAP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scan() {
        ESPDevice dev = ProvisionSession.get().getEspDevice();
        if (dev == null) {
            // Nếu chưa có thiết bị, tự tìm và kết nối SoftAP trước, rồi mới quét WiFi
            discoverAndConnectSoftAp();
            return;
        }
        adapter.clear();
        adapter.add(getString(R.string.wifi_scanning));
        aps.clear();
        try {
            dev.scanNetworks(new WiFiScanListener() {
                @Override public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    runOnUiThread(() -> updateList(wifiList));
                }
                @Override public void onWiFiScanFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(WiFiProvisionActivity.this, getString(R.string.wifi_scan_failed, e.getMessage()), Toast.LENGTH_SHORT).show());
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, getString(R.string.wifi_scan_failed, se.getMessage()), Toast.LENGTH_SHORT).show();
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
                    runOnUiThread(() -> {
                        Toast.makeText(WiFiProvisionActivity.this, R.string.provision_success, Toast.LENGTH_LONG).show();
                        // Add to inventory similarly
                        String name = "ESP Device";
                        Device newDevice = new Device(UUID.randomUUID().toString(), name, "ESP Device", false);
                        newDevice.setToken(ssid);
                        newDevice.addCaps(Device.CAP_POWER, Device.CAP_BRIGHTNESS, Device.CAP_COLOR);
                        newDevice.setBrightness(100);
                        newDevice.setColor(0xFFFFFFFF);
                        SmartRepository.get(WiFiProvisionActivity.this).addToInventory(newDevice);
                        if (dismissOnSuccess != null) dismissOnSuccess.dismiss();
                        finish();
                    });
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, getString(R.string.provision_failed, se.getMessage()), Toast.LENGTH_SHORT).show();
        }
    }
}
