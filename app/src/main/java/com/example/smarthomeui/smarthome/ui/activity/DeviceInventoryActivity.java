package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.le.ScanResult;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.DeviceInventoryAdapter;
import com.example.smarthomeui.smarthome.components.DeviceControlBottomSheet;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.provision.ProvisionSession;

// Import đầy đủ các class cần thiết
import com.espressif.provisioning.ESPProvisionManager;
import com.espressif.provisioning.ESPDevice;
import com.espressif.provisioning.listeners.BleScanListener;
import com.espressif.provisioning.listeners.WiFiScanListener;
import com.espressif.provisioning.ESPConstants;
import com.espressif.provisioning.WiFiAccessPoint;
import com.espressif.provisioning.listeners.ProvisionListener;

import java.util.*;

import static com.example.smarthomeui.smarthome.model.Device.*;

public class DeviceInventoryActivity extends AppCompatActivity {

    private final List<Device> inventory = new ArrayList<>();
    private DeviceInventoryAdapter adapter;

    // Enum nội bộ để thay thế cho TransportType và SecurityType
    private enum DeviceTransportType {
        BLE, SOFTAP
    }

    private static final int REQ_PERMS = 1001;
    @Nullable private DeviceTransportType pendingTransportType;

    private static final String ESP_BLE_PRIMARY_SERVICE_UUID = "0000ffff-0000-1000-8000-00805f9b34fb";
    @Nullable private ESPDevice currentEspDevice;

    // Maintain a live list dialog for BLE scan results
    private final List<BluetoothDevice> bleDevices = new ArrayList<>();
    @Nullable private AlertDialog bleListDialog;
    @Nullable private ArrayAdapter<String> bleListAdapter;

    // Helper: safely get device name without throwing SecurityException on S+
    private String getDeviceDisplayName(@Nullable BluetoothDevice device) {
        if (device == null) return "Unknown";
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!hasPerm(Manifest.permission.BLUETOOTH_CONNECT)) {
                    return "Unknown";
                }
            }
            String n = device.getName();
            return (n == null || n.isEmpty()) ? "Unknown" : n;
        } catch (SecurityException se) {
            return "Unknown";
        }
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_inventory);
        findViewById(R.id.ivDevices).setSelected(true);

        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));

        // Plus ở giữa: mở dialog thêm thiết bị vào KHO (không gán phòng)
        findViewById(R.id.ivPlus).setOnClickListener(v -> openAddToInventoryDialog());

        findViewById(R.id.ivSetting).setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        findViewById(R.id.ivBack).setOnClickListener(v -> onBackPressed());

        RecyclerView rv = findViewById(R.id.rvInventory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        inventory.addAll(SmartRepository.get(this).getInventory());
        adapter = new DeviceInventoryAdapter(inventory, new DeviceInventoryAdapter.OnItemAction() {
            @Override public void onControl(Device d, int pos) {
                // dùng lại bottom sheet điều khiển theo capabilities (tuỳ chọn)
                DeviceControlBottomSheet.newInstance(d, changed -> adapter.notifyItemChanged(pos))
                        .show(getSupportFragmentManager(), "control");
            }
            @Override public void onAssign(Device d, int pos) {
                // TODO: mở dialog chọn Nhà/Phòng rồi gọi SmartRepository.assignInventoryDeviceToRoom(...)
                // (phần gán này bạn bảo khi nào cần mình gửi thêm)
            }
            @Override public void onDelete(Device d, int pos) {
                SmartRepository.get(DeviceInventoryActivity.this).removeFromInventory(d.getId());
                inventory.remove(pos);
                adapter.notifyItemRemoved(pos);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.fabAddInventory).setOnClickListener(v -> openAddToInventoryDialog());
    }
    private void openAddToInventoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ThemeOverlay_Material3_Dialog);
        builder.setTitle("Thêm thiết bị ESP");

        String[] options = {"Quét thiết bị BLE", "Quét thiết bị WiFi (SoftAP)", "Thêm thủ công"};

        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Open full-screen BLE scan UI
                    startActivity(new Intent(this, BLEScanActivity.class));
                    break;
                case 1:
                    // Open WiFi provision activity directly (has built-in WiFi scanning)
                    startActivity(new Intent(this, WiFiProvisionActivity.class));
                    break;
                case 2:
                    openManualAddDialog();
                    break;
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void startSoftApProvisionFlow() {
        try {
            if (!checkPermissions()) {
                pendingTransportType = DeviceTransportType.SOFTAP;
                requestPermissions(DeviceTransportType.SOFTAP);
                return;
            }

            Toast.makeText(this, "Đang tìm thiết bị SoftAP...", Toast.LENGTH_SHORT).show();

            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);

            // Tìm kiếm các thiết bị WiFi ESP trước
            pm.searchWiFiEspDevices("", new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    runOnUiThread(() -> {
                        if (wifiList == null || wifiList.isEmpty()) {
                            Toast.makeText(DeviceInventoryActivity.this, "Không tìm thấy thiết bị ESP SoftAP", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showSoftApDevicesList(wifiList);
                    });
                }

                @Override
                public void onWiFiScanFailed(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(DeviceInventoryActivity.this, "Quét SoftAP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Không thể bắt đầu SoftAP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showSoftApDevicesList(ArrayList<WiFiAccessPoint> wifiList) {
        String[] deviceNames = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            try {
                deviceNames[i] = wifiList.get(i).getWifiName();
            } catch (Exception e) {
                deviceNames[i] = "ESP Device " + i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn thiết bị ESP SoftAP")
                .setItems(deviceNames, (dialog, which) -> {
                    connectToSoftApDevice(deviceNames[which]);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void connectToSoftApDevice(String deviceName) {
        try {
            Toast.makeText(this, "Đang kết nối đến " + deviceName + "...", Toast.LENGTH_SHORT).show();

            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);
            ESPDevice esp = pm.createESPDevice(ESPConstants.TransportType.TRANSPORT_SOFTAP,
                                               ESPConstants.SecurityType.SECURITY_2);

            // Set proof of possession
            try {
                esp.setProofOfPossession("abcd1234");
            } catch (Exception ignored) {}

            // Kết nối đến thiết bị
            try {
                esp.connectToDevice();
            } catch (SecurityException se) {
                Toast.makeText(this, "Thiếu quyền mạng để kết nối SoftAP: " + se.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu ESP device vào session và chuyển đến WiFi provision
            ProvisionSession.get().setEspDevice(esp);

            // Mở WiFi provisioning activity
            WiFiProvisionActivity.start(this);

        } catch (Exception e) {
            Toast.makeText(this, "Kết nối SoftAP thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ESP_SOFTAP", "connectToSoftApDevice error", e);
        }
    }

    private void startESPDeviceScan(DeviceTransportType transportType) {
        if (!checkPermissions()) {
            pendingTransportType = transportType;
            requestPermissions(transportType);
            return;
        }

        try {
            ESPProvisionManager provisionManager = ESPProvisionManager.getInstance(this);

            if (transportType == DeviceTransportType.BLE) {
                Toast.makeText(this, "Đang quét thiết bị BLE...", Toast.LENGTH_SHORT).show();

                // Prepare and show a single picker dialog that updates as devices are found
                runOnUiThread(() -> showBleDevicesPicker());

                try {
                    provisionManager.searchBleEspDevices("", new BleScanListener() {
                        @Override
                        public void scanStartFailed() {
                            runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this,
                                    "Không thể bắt đầu quét BLE", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onPeripheralFound(BluetoothDevice device, ScanResult scanResult) {
                            runOnUiThread(() -> addBleDeviceToList(device));
                        }

                        @Override
                        public void scanCompleted() {
                            runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this,
                                    "Quét BLE hoàn tất", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this,
                                    "Lỗi quét BLE: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                } catch (SecurityException se) {
                    Toast.makeText(this, "Thiếu quyền BLE (SCAN)", Toast.LENGTH_SHORT).show();
                    pendingTransportType = transportType;
                    requestPermissions(transportType);
                }
            } else {
                Toast.makeText(this, "Đang quét thiết bị WiFi (SoftAP)...", Toast.LENGTH_SHORT).show();
                try {
                    provisionManager.searchWiFiEspDevices("", new WiFiScanListener() {
                        @Override
                        public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                            runOnUiThread(() -> showWiFiDevicesList(wifiList, transportType));
                        }

                        @Override
                        public void onWiFiScanFailed(Exception e) {
                            runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this,
                                    "Lỗi quét SoftAP: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                } catch (SecurityException se) {
                    Toast.makeText(this, "Thiếu quyền WiFi", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo ESP Manager: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ESP_SCAN", "Error initializing ESP Manager", e);
        }
    }

    // Show a single BLE devices picker dialog and keep it updated
    private void showBleDevicesPicker() {
        // Reset list and adapter
        bleDevices.clear();
        List<String> names = new ArrayList<>();
        bleListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, names);

        AlertDialog.Builder b = new AlertDialog.Builder(this)
                .setTitle("Chọn thiết bị BLE")
                .setAdapter(bleListAdapter, (d, which) -> {
                    if (which >= 0 && which < bleDevices.size()) {
                        BluetoothDevice sel = bleDevices.get(which);
                        d.dismiss();
                        connectBleToDevice(sel);
                    }
                })
                .setNegativeButton("Đóng", (d, w) -> d.dismiss());
        bleListDialog = b.create();
        bleListDialog.show();

        // If nothing found yet, show a hint row
        if (bleListAdapter.getCount() == 0) {
            bleListAdapter.add("Đang quét...");
        }
    }

    private void addBleDeviceToList(BluetoothDevice device) {
        if (device == null) return;
        // Avoid duplicates by MAC address
        String addr = device.getAddress();
        for (BluetoothDevice d : bleDevices) {
            if (Objects.equals(d.getAddress(), addr)) {
                return;
            }
        }
        bleDevices.add(device);

        if (bleListAdapter != null) {
            String name = getDeviceDisplayName(device);
            String display = name + " (" + addr + ")";
            // Remove placeholder if present
            int idx = -1;
            for (int i = 0; i < bleListAdapter.getCount(); i++) {
                if (Objects.equals(bleListAdapter.getItem(i), "Đang quét...")) { idx = i; break; }
            }
            if (idx >= 0) bleListAdapter.remove("Đang quét...");
            bleListAdapter.add(display);
            bleListAdapter.notifyDataSetChanged();
        } else {
            // If adapter not ready yet, ensure dialog is shown
            showBleDevicesPicker();
        }
    }

    private void showBleDeviceDialog(BluetoothDevice device) {
        // Replaced by list-based picker; keep for backward-compat if needed
        String name = getDeviceDisplayName(device);
        new AlertDialog.Builder(this)
                .setTitle("Thiết bị BLE tìm thấy")
                .setMessage("Tên: " + name + "\nĐịa chỉ: " + device.getAddress())
                .setPositiveButton("Kết nối", (d, w) -> connectBleToDevice(device))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void connectBleToDevice(BluetoothDevice device) {
        try {
            if (!checkPermissions()) {
                pendingTransportType = DeviceTransportType.BLE;
                requestPermissions(DeviceTransportType.BLE);
                return;
            }
            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);
            ESPDevice esp = pm.createESPDevice(ESPConstants.TransportType.TRANSPORT_BLE,
                                               ESPConstants.SecurityType.SECURITY_2);
            currentEspDevice = esp;
            try { esp.setProofOfPossession("abcd1234"); } catch (Exception ignored) {}
            esp.connectBLEDevice(device, ESP_BLE_PRIMARY_SERVICE_UUID);
            // Dismiss the list dialog if showing
            if (bleListDialog != null && bleListDialog.isShowing()) {
                bleListDialog.dismiss();
            }
            showWifiScanAndProvision();
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền BLE", Toast.LENGTH_SHORT).show();
            pendingTransportType = DeviceTransportType.BLE;
            requestPermissions(DeviceTransportType.BLE);
        } catch (Exception e) {
            Toast.makeText(this, "Kết nối BLE thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ESP_BLE", "connectBleToDevice error", e);
        }
    }

    private void showWifiScanAndProvision() {
        if (currentEspDevice == null) {
            Toast.makeText(this, "Thiết bị ESP chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đang quét WiFi gần thiết bị...", Toast.LENGTH_SHORT).show();
        try {
            currentEspDevice.scanNetworks(new WiFiScanListener() {
                @Override
                public void onWifiListReceived(ArrayList<WiFiAccessPoint> wifiList) {
                    runOnUiThread(() -> showProvisionChoiceDialog(wifiList));
                }
                @Override
                public void onWiFiScanFailed(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(DeviceInventoryActivity.this, "Quét WiFi thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Cho nhập tay nếu quét lỗi
                        showManualProvisionDialog(null);
                    });
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền Location/WiFi để quét mạng", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProvisionChoiceDialog(@Nullable ArrayList<WiFiAccessPoint> wifiList) {
        List<String> entries = new ArrayList<>();
        if (wifiList != null) {
            for (WiFiAccessPoint ap : wifiList) {
                try { entries.add(ap.getWifiName()); } catch (Exception e) { entries.add(String.valueOf(ap)); }
            }
        }
        entries.add("Nhập SSID / mật khẩu thủ công");
        String[] items = entries.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle("Chọn mạng WiFi để cung cấp")
                .setItems(items, (d, which) -> {
                    if (wifiList != null && which < wifiList.size()) {
                        String ssid = items[which];
                        showManualProvisionDialog(ssid);
                    } else {
                        showManualProvisionDialog(null);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showManualProvisionDialog(@Nullable String prefillSsid) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_provision_wifi, null, false);
        EditText edtSsid = view.findViewById(R.id.edtSsid);
        EditText edtPass = view.findViewById(R.id.edtPass);
        if (prefillSsid != null) edtSsid.setText(prefillSsid);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_Material3_Dialog)
                .setTitle("Cấu hình WiFi cho thiết bị")
                .setView(view)
                .setPositiveButton("Cấu hình", null)
                .setNegativeButton("Hủy", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(di -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String ssid = String.valueOf(edtSsid.getText()).trim();
                String pass = String.valueOf(edtPass.getText());
                if (ssid.isEmpty()) { edtSsid.setError("Nhập SSID"); return; }
                provisionToNetwork(ssid, pass, dialog);
            });
        });
        dialog.show();
    }

    private void provisionToNetwork(String ssid, String pass, AlertDialog dialogToDismiss) {
        if (currentEspDevice == null) {
            Toast.makeText(this, "Thiết bị ESP chưa sẵn sàng", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Đang gửi cấu hình WiFi...", Toast.LENGTH_SHORT).show();
        try {
            currentEspDevice.provision(ssid, pass, new ProvisionListener() {
                @Override
                public void createSessionFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Tạo phiên thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                @Override
                public void wifiConfigSent() {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Đã gửi cấu hình WiFi", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void wifiConfigFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Gửi cấu hình thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                @Override
                public void wifiConfigApplied() {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Thiết bị áp dụng cấu hình", Toast.LENGTH_SHORT).show());
                }
                @Override
                public void wifiConfigApplyFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Áp dụng cấu hình thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                @Override
                public void provisioningFailedFromDevice(ESPConstants.ProvisionFailureReason failureReason) {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Provisioning thất bại từ thiết bị: " + failureReason, Toast.LENGTH_SHORT).show());
                }
                @Override
                public void onProvisioningFailed(Exception e) {
                    runOnUiThread(() -> Toast.makeText(DeviceInventoryActivity.this, "Provisioning thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
                @Override
                public void deviceProvisioningSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(DeviceInventoryActivity.this, "Provisioning thành công!", Toast.LENGTH_LONG).show();
                        // Sau khi provision thành công, thêm vào inventory nếu chưa có
                        String name = "ESP Device";
                        Device newDevice = new Device(UUID.randomUUID().toString(), name, "ESP Device", false);
                        newDevice.setToken(ssid);
                        newDevice.addCaps(CAP_POWER, CAP_BRIGHTNESS, CAP_COLOR);
                        newDevice.setBrightness(100);
                        newDevice.setColor(0xFFFFFFFF);
                        SmartRepository.get(DeviceInventoryActivity.this).addToInventory(newDevice);
                        inventory.add(newDevice);
                        adapter.notifyItemInserted(inventory.size() - 1);
                        if (dialogToDismiss != null) dialogToDismiss.dismiss();
                    });
                }
            });
        } catch (SecurityException se) {
            Toast.makeText(this, "Thiếu quyền mạng/Location để provisioning", Toast.LENGTH_SHORT).show();
        }
    }

    // Sửa lại connectToESPDevice cho SoftAP: sau khi kết nối, cũng chuyển sang quét WiFi
    private void connectToESPDevice(String deviceName, String deviceAddress, DeviceTransportType transportType) {
        try {
            Toast.makeText(this, "Đang kết nối đến " + deviceName + "...", Toast.LENGTH_SHORT).show();
            if (!checkPermissions()) {
                pendingTransportType = transportType;
                requestPermissions(transportType);
                return;
            }
            ESPProvisionManager pm = ESPProvisionManager.getInstance(this);
            ESPConstants.TransportType tt = (transportType == DeviceTransportType.BLE)
                    ? ESPConstants.TransportType.TRANSPORT_BLE
                    : ESPConstants.TransportType.TRANSPORT_SOFTAP;
            ESPDevice esp = pm.createESPDevice(tt, ESPConstants.SecurityType.SECURITY_2);
            currentEspDevice = esp;
            try { esp.setProofOfPossession("abcd1234"); } catch (Exception ignored) {}
            if (transportType == DeviceTransportType.BLE) {
                // Flow BLE dùng connectBleToDevice thay thế
                Toast.makeText(this, "Vui lòng chọn từ danh sách BLE để kết nối", Toast.LENGTH_SHORT).show();
                return;
            } else {
                // SoftAP: thư viện sẽ tự kết nối tới SSID thiết bị thông qua connectToDevice()
                try {
                    esp.connectToDevice();
                } catch (SecurityException se) {
                    Toast.makeText(this, "Thiếu quyền mạng để kết nối SoftAP", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Sau khi kết nối thành công, quét WiFi và provisioning
                showWifiScanAndProvision();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Kết nối thiết bị thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ESP_CONNECT", "connectToESPDevice error", e);
        }
    }

    private void openManualAddDialog() {
        // Giữ lại dialog thêm thủ công cũ cho trường hợp cần thiết
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_device_global, null, false);

        EditText edtName = view.findViewById(R.id.edtDeviceName);
        EditText edtToken = view.findViewById(R.id.edtDeviceToken);
        com.google.android.material.chip.ChipGroup chipGroupType = view.findViewById(R.id.chipGroupType);

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_Material3_Dialog)
                .setView(view).create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name = String.valueOf(edtName.getText()).trim();
            String token = String.valueOf(edtToken.getText()).trim();

            if (name.isEmpty()) { edtName.setError("Nhập tên thiết bị"); return; }
            if (token.isEmpty()) { edtToken.setError("Nhập device token"); return; }
            if (!token.matches("^[A-Za-z0-9_-]{6,64}$")) {
                edtToken.setError("Token không hợp lệ (6–64 ký tự)"); return;
            }

            // Lấy type từ ChipGroup
            int checkedId = chipGroupType.getCheckedChipId();
            if (checkedId == View.NO_ID) return;
            com.google.android.material.chip.Chip chip = view.findViewById(checkedId);
            String type = chip.getText().toString();

            Device dev = new Device(UUID.randomUUID().toString(), name, type, false);
            dev.setToken(token);

            String lower = type.toLowerCase(Locale.US);
            if (lower.contains("light")) {
                dev.addCaps(CAP_POWER, CAP_BRIGHTNESS, CAP_COLOR);
                dev.setBrightness(100);
                dev.setColor(0xFFFFFFFF);
            } else if (lower.contains("fan")) {
                dev.addCaps(CAP_POWER, CAP_SPEED);
                dev.setSpeed(1);
            } else if (lower.equals("ac")) {
                dev.addCaps(CAP_POWER, CAP_TEMPERATURE, CAP_SPEED);
                dev.setTemperature(25);
                dev.setSpeed(1);
            } else {
                dev.addCaps(CAP_POWER);
            }

            SmartRepository.get(this).addToInventory(dev);
            inventory.add(dev);
            adapter.notifyItemInserted(inventory.size()-1);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showWiFiDevicesList(ArrayList<WiFiAccessPoint> wifiList, DeviceTransportType transportType) {
        if (wifiList == null || wifiList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy SoftAP của ESP", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] ssids = new String[wifiList.size()];
        for (int i = 0; i < wifiList.size(); i++) {
            try {
                ssids[i] = wifiList.get(i).getWifiName();
            } catch (Exception ignore) {
                ssids[i] = String.valueOf(wifiList.get(i));
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Chọn SoftAP của ESP")
                .setItems(ssids, (dialog, which) -> {
                    String ssid = ssids[which];
                    connectToESPDevice(ssid, ssid, transportType);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return hasPerm(Manifest.permission.BLUETOOTH_SCAN) &&
                   hasPerm(Manifest.permission.BLUETOOTH_CONNECT);
        } else {
            return hasPerm(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private boolean hasPerm(String p) {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions(DeviceTransportType transportType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            }, REQ_PERMS);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION
            }, REQ_PERMS);
        }
    }

    private void requestPermissions() {
        requestPermissions(DeviceTransportType.BLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMS) {
            boolean allGranted = true;
            for (int r : grantResults) {
                if (r != PackageManager.PERMISSION_GRANTED) { allGranted = false; break; }
            }
            if (allGranted && pendingTransportType != null) {
                DeviceTransportType tmp = pendingTransportType;
                pendingTransportType = null;
                startESPDeviceScan(tmp);
            } else if (!allGranted) {
                Toast.makeText(this, "Cần cấp quyền để quét/kết nối thiết bị ESP", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
