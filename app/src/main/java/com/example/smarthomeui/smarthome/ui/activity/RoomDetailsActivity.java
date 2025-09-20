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
    private List<Device> devices; // list an toàn để tránh NPE

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Phải trỏ đúng layout mới bạn đã thay (có tvRoomTitle, ivBack, rvDevices, fabAddDevice)
        setContentView(R.layout.activity_room_details);

        houseId = getIntent().getStringExtra("house_id");
        roomId  = getIntent().getStringExtra("room_id");
        room    = SmartRepository.get(this).getRoomById(houseId, roomId);

        // Header: title + back
        TextView tvTitle = findViewById(R.id.tvRoomTitle);
        View ivBack = findViewById(R.id.ivBack);
        if (tvTitle != null) {
            tvTitle.setText(room != null ? room.getName() : getString(R.string.app_name));
        }
        if (ivBack != null) {
            ivBack.setOnClickListener(v -> onBackPressed());
        }

        // RecyclerView
        rv = findViewById(R.id.rvDevices);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Dữ liệu thiết bị (an toàn null)
        devices = (room != null && room.getDevices() != null)
                ? room.getDevices()
                : new ArrayList<>();

        adapter = new SingleRoomAdapter(devices);
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
                    if (name.isEmpty()) return;

                    // Nếu room null (không có context), bỏ qua để tránh crash
                    if (room == null) return;

                    Device dev = new Device(UUID.randomUUID().toString(), name, type, false);
                    // Lưu vào repo
                    SmartRepository.get(this).addDevice(houseId, roomId, dev);

                    // Cập nhật UI
                    int newPos = devices.size() - 1; // vì addDevice đã thêm vào list room.getDevices()
                    if (newPos < 0) newPos = 0;
                    adapter.notifyItemInserted(newPos);
                    rv.smoothScrollToPosition(newPos);
                })
                .show();
    }
}
