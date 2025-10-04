package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.DeviceInventoryAdapter;
import com.example.smarthomeui.smarthome.components.DeviceControlBottomSheet;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.Device;

import java.util.*;

import static com.example.smarthomeui.smarthome.model.Device.*;

public class DeviceInventoryActivity extends AppCompatActivity {

    private final List<Device> inventory = new ArrayList<>();
    private DeviceInventoryAdapter adapter;

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
        // dialog sử dụng background đồng bộ + Name, Token, Type (dùng ChipGroup hoặc AutoComplete)
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_device_global, null, false);

        EditText edtName  = view.findViewById(R.id.edtDeviceName);
        EditText edtToken = view.findViewById(R.id.edtDeviceToken);

        // Nếu bạn đang dùng ChipGroup cho Type:
        com.google.android.material.chip.ChipGroup chipGroupType = view.findViewById(R.id.chipGroupType);
        // Hoặc nếu dùng AutoCompleteTextView (actType) thì lấy text từ đó.

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.ThemeOverlay_Material3_Dialog)
                .setView(view).create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnAdd).setOnClickListener(v -> {
            String name  = String.valueOf(edtName.getText()).trim();
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
            dev.setToken(token); // Thêm set token
            // map capabilities theo type
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
            } else { // Outlet / default
                dev.addCaps(CAP_POWER);
            }

            SmartRepository.get(this).addToInventory(dev);
            inventory.add(dev);
            adapter.notifyItemInserted(inventory.size()-1);
            dialog.dismiss();
        });

        dialog.show();
    }
}
