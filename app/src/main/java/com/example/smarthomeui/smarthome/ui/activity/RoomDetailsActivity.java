package com.example.smarthomeui.smarthome.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.SingleRoomAdapter;
import com.example.smarthomeui.smarthome.components.DeviceControlBottomSheet;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.model.Room;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoomDetailsActivity extends AppCompatActivity {

    private String houseId;
    private String roomId;
    private Room room;

    private RecyclerView rv;
    private SingleRoomAdapter adapter;
    private List<Device> devices;   // tham chiếu list thiết bị của room

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        houseId = getIntent().getStringExtra("house_id");
        roomId  = getIntent().getStringExtra("room_id");
        room    = SmartRepository.get(this).getRoomById(houseId, roomId);

        // Header
        TextView tvTitle = findViewById(R.id.tvRoomTitle);
        View ivBack = findViewById(R.id.ivBack);
        if (tvTitle != null) tvTitle.setText(room != null ? room.getName() : getString(R.string.app_name));
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // RecyclerView
        rv = findViewById(R.id.rvDevices);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Lấy danh sách thiết bị an toàn
        devices = (room != null && room.getDevices() != null) ? room.getDevices() : new ArrayList<>();

        // Adapter + click mở điều khiển
        adapter = new SingleRoomAdapter(devices, (device, pos) -> {
            DeviceControlBottomSheet.newInstance(device, changed -> {
                int idx = pos;
                if (idx < 0 || idx >= devices.size()) idx = devices.indexOf(changed);
                if (idx >= 0) adapter.notifyItemChanged(idx);
            }).show(getSupportFragmentManager(), "device_control");
        });

        rv.setAdapter(adapter);

        // FAB thêm thiết bị
        FloatingActionButton fab = findViewById(R.id.fabAddDevice);
        if (fab != null) fab.setOnClickListener(v -> openAddDeviceDialog());
    }

    private void openAddDeviceDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_device, null, false);
        EditText edtName = view.findViewById(R.id.edtDeviceName);
        Spinner spType   = view.findViewById(R.id.spDeviceType);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Thêm thiết bị")
                .setView(view)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Thêm", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String type = String.valueOf(spType.getSelectedItem());
                    if (name.isEmpty() || room == null) return;

                    // Tạo device mẫu (đặt sáng 100% nếu là đèn)
                    Device dev = new Device(UUID.randomUUID().toString(), name, type, false);
                    if (type.equalsIgnoreCase("Light")) dev.setBrightness(100);

                    SmartRepository.get(this).addDevice(houseId, roomId, dev);

                    int newPos = devices.size() - 1;   // đã được add vào list của room
                    if (newPos < 0) newPos = 0;
                    adapter.notifyItemInserted(newPos);
                    rv.smoothScrollToPosition(newPos);
                })
                .show();
    }
}
