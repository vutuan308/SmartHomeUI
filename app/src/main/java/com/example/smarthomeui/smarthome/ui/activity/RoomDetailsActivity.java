package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
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

public class RoomDetailsActivity extends AppCompatActivity {

    private String houseId;
    private String roomId;
    private Room room;

    private RecyclerView rv;
    private SingleRoomAdapter adapter;
    private List<Device> devices;   // list thiết bị thuộc phòng

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

        devices = (room != null && room.getDevices() != null) ? room.getDevices() : new ArrayList<>();

        adapter = new SingleRoomAdapter(devices, (device, pos) -> {
            DeviceControlBottomSheet.newInstance(device, changed -> {
                int idx = pos;
                if (idx < 0 || idx >= devices.size()) idx = devices.indexOf(changed);
                if (idx >= 0) adapter.notifyItemChanged(idx);
            }).show(getSupportFragmentManager(), "device_control");
        });
        rv.setAdapter(adapter);

        // FAB: LẤY THIẾT BỊ TỪ KHO
        FloatingActionButton fab = findViewById(R.id.fabAddDevice);
        if (fab != null) fab.setOnClickListener(v -> openPickFromInventory());
    }

    /** Mở dialog chọn 1 thiết bị từ Kho và gán vào phòng */
    private void openPickFromInventory() {
        List<Device> inv = SmartRepository.get(this).getInventory();
        if (inv == null || inv.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Kho trống")
                    .setMessage("Hiện chưa có thiết bị nào trong Kho. Bạn muốn mở Kho để thêm mới không?")
                    .setNegativeButton("Đóng", null)
                    .setPositiveButton("Mở Kho", (d, w) ->
                            startActivity(new Intent(this, DeviceInventoryActivity.class)))
                    .show();
            return;
        }

        String[] items = new String[inv.size()];
        for (int i = 0; i < inv.size(); i++) {
            Device di = inv.get(i);
            items[i] = di.getName() + " • " + (di.getType() == null ? "Unknown" : di.getType());
        }
        final int[] selected = {0};

        new AlertDialog.Builder(this)
                .setTitle("Chọn thiết bị từ Kho")
                .setSingleChoiceItems(items, 0, (dlg, which) -> selected[0] = which)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Thêm vào phòng", (dlg, w) -> {
                    Device picked = inv.get(selected[0]);

                    // Gán vào phòng (sẽ tự động xóa khỏi inventory)
                    boolean success = SmartRepository.get(this).assignInventoryDeviceToRoom(
                            picked.getId(), houseId, roomId
                    );

                    if (success) {
                        // Cập nhật UI (thiết bị đã được thêm vào list của room)
                        int newPos = devices.size() - 1;
                        if (newPos < 0) newPos = 0;
                        adapter.notifyItemInserted(newPos);
                        rv.smoothScrollToPosition(newPos);
                    }
                })
                .show();
    }

    /* Nếu vẫn muốn giữ dialog tạo “thiết bị mới” thì để lại hàm cũ,
       còn bây giờ đã chuyển sang lấy từ Kho nên không dùng nữa. */
    @SuppressWarnings("unused")
    private void openAddDeviceDialog_OLD() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_device, null, false);
        EditText edtName = view.findViewById(R.id.edtDeviceName);
        Spinner spType   = view.findViewById(R.id.spDeviceType);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(
                this, R.array.device_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(typeAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Thêm thiết bị (cũ)")
                .setView(view)
                .setNegativeButton("Huỷ", null)
                .setPositiveButton("Thêm", (d, w) -> {
                    String name = edtName.getText().toString().trim();
                    String type = String.valueOf(spType.getSelectedItem());
                    if (name.isEmpty() || room == null) return;

                    Device dev = new Device(java.util.UUID.randomUUID().toString(), name, type, false);
                    if ("Light".equalsIgnoreCase(type)) dev.setBrightness(100);

                    SmartRepository.get(this).addDevice(houseId, roomId, dev);

                    int newPos = devices.size() - 1;
                    if (newPos < 0) newPos = 0;
                    adapter.notifyItemInserted(newPos);
                    rv.smoothScrollToPosition(newPos);
                })
                .show();
    }
}
