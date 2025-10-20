package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.SingleRoomAdapter;
import com.example.smarthomeui.smarthome.components.DeviceControlBottomSheet;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.model.Room;
import com.example.smarthomeui.smarthome.network.Api;
import com.example.smarthomeui.smarthome.network.ApiClient;
import com.example.smarthomeui.smarthome.network.DeviceDto;
import com.example.smarthomeui.smarthome.network.DeviceListWrap;
import com.example.smarthomeui.smarthome.network.RoomDto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoomDetailsActivity extends AppCompatActivity {

    private String houseId;
    private String roomId;
    private Room room;

    private RecyclerView rv;
    private SingleRoomAdapter adapter;
    private List<Device> devices;   // list thiết bị thuộc phòng
    private TextView tvTitle;       // Reference to title TextView

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_details);

        houseId = getIntent().getStringExtra("house_id");
        roomId  = getIntent().getStringExtra("room_id");


        // Header
        tvTitle = findViewById(R.id.tvRoomTitle);
        View ivBack = findViewById(R.id.ivBack);
        if (tvTitle != null) tvTitle.setText(room != null ? room.getName() : getString(R.string.app_name));
        if (ivBack != null) ivBack.setOnClickListener(v -> onBackPressed());

        // RecyclerView
        rv = findViewById(R.id.rvDevices);
        rv.setLayoutManager(new LinearLayoutManager(this));

        devices = new ArrayList<>();

        adapter = new SingleRoomAdapter(devices, (device, pos) -> {
            DeviceControlBottomSheet.newInstance(device, changed -> {
                int idx = pos;
                if (idx < 0 || idx >= devices.size()) idx = devices.indexOf(changed);
                if (idx >= 0) adapter.notifyItemChanged(idx);
            }).show(getSupportFragmentManager(), "device_control");
        });
        rv.setAdapter(adapter);

        // Load room info and devices from API
        loadRoomFromAPI();
        loadDevicesFromAPI();

        // FAB: LẤY THIẾT BỊ TỪ KHO
        FloatingActionButton fab = findViewById(R.id.fabAddDevice);
        if (fab != null) fab.setOnClickListener(v -> openPickFromInventory());
    }

    /** Load room information from API */
    private void loadRoomFromAPI() {
        // Parse roomId to int for API call
        int apiRoomId;
        try {
            apiRoomId = Integer.parseInt(roomId);
        } catch (NumberFormatException e) {
            // If roomId is not a number, use local data
            return;
        }

        Api api = ApiClient.getClient(this).create(Api.class);
        Call<RoomDto> call = api.getRoomById(apiRoomId);

        call.enqueue(new Callback<RoomDto>() {
            @Override
            public void onResponse(Call<RoomDto> call, Response<RoomDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RoomDto roomDto = response.body();

                    // Update room title with name from API
                    if (tvTitle != null && roomDto.name != null) {
                        tvTitle.setText(roomDto.name);
                    }
                }
                // If API fails, keep the current title from local data
            }

            @Override
            public void onFailure(Call<RoomDto> call, Throwable t) {
                // Keep using local room name if API fails
            }
        });
    }

    /** Load devices from API by room ID */
    private void loadDevicesFromAPI() {
        // Parse roomId to int for API call
        int apiRoomId;
        try {
            apiRoomId = Integer.parseInt(roomId);
        } catch (NumberFormatException e) {
            // If roomId is not a number, fallback to local data
            loadDevicesFromLocal();
            return;
        }

        Api api = ApiClient.getClient(this).create(Api.class);
        Call<DeviceListWrap> call = api.getDevicesByRoomId(apiRoomId);

        call.enqueue(new Callback<DeviceListWrap>() {
            @Override
            public void onResponse(Call<DeviceListWrap> call, Response<DeviceListWrap> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DeviceListWrap deviceWrap = response.body();
                    List<DeviceDto> apiDevices = deviceWrap.getDevices();

                    if (apiDevices != null && !apiDevices.isEmpty()) {
                        devices.clear();

                        // Convert API devices to local Device model
                        for (DeviceDto deviceDto : apiDevices) {
                            Device device = convertApiDeviceToDevice(deviceDto);
                            devices.add(device);
                        }

                        // Update RecyclerView
                        adapter.notifyDataSetChanged();
                    } else {
                        // No devices found, show empty state
                        devices.clear();
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    // API call failed, fallback to local data
                    Toast.makeText(RoomDetailsActivity.this,
                            "Không thể tải từ server, hiển thị dữ liệu local",
                            Toast.LENGTH_SHORT).show();
                    loadDevicesFromLocal();
                }
            }

            @Override
            public void onFailure(Call<DeviceListWrap> call, Throwable t) {
                // Network error, fallback to local data
                Toast.makeText(RoomDetailsActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
                loadDevicesFromLocal();
            }
        });
    }

    /** Fallback: Load devices from local repository */
    private void loadDevicesFromLocal() {
        devices.clear();
        if (room != null && room.getDevices() != null) {
            devices.addAll(room.getDevices());
        }
        adapter.notifyDataSetChanged();
    }

    /** Convert DeviceDto from API to local Device model */
    private Device convertApiDeviceToDevice(DeviceDto deviceDto) {
        Device device = new Device(
                String.valueOf(deviceDto.getId()),
                deviceDto.getName(),
                room != null ? room.getName() : "",
                deviceDto.getType() != null ? deviceDto.getType() : "Unknown",
                false, // default to off
                0,     // default power
                "N/A"  // default wattage
        );

        // Store API ID as token for future reference
        device.setToken(String.valueOf(deviceDto.getId()));

        // Set default properties based on type
        if ("Light".equalsIgnoreCase(deviceDto.getType())) {
            device.setBrightness(100);
            device.setColor(0xFFFFFFFF);
        } else if ("Fan".equalsIgnoreCase(deviceDto.getType())) {
            device.setSpeed(1);
        }

        return device;
    }

    /** Mở dialog chọn 1 thiết bị từ Kho và gán vào phòng */
    private void openPickFromInventory() {

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


                    int newPos = devices.size() - 1;
                    if (newPos < 0) newPos = 0;
                    adapter.notifyItemInserted(newPos);
                    rv.smoothScrollToPosition(newPos);
                })
                .show();
    }
}
