package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.RoomAdapter;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.House;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.List;

public class HouseRoomsActivity extends AppCompatActivity {
    private String houseId;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_rooms);
        View back = findViewById(R.id.ivBack);
        if (back != null) back.setOnClickListener(v -> onBackPressed());

        houseId = getIntent().getStringExtra("house_id");
        House h = SmartRepository.get(this).getHouseById(houseId);
        setTitle(h != null ? h.getName() : "Phòng");
        TextView title = findViewById(R.id.tvHouseTitle);
        if (title != null && h != null) title.setText(h.getName());

        RecyclerView rv = findViewById(R.id.rvRoomsOfHouse);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        final List<Room> rooms = SmartRepository.get(this).getRooms(houseId);

        RoomAdapter adapter = new RoomAdapter(rooms, room -> {
            Intent i = new Intent(HouseRoomsActivity.this, RoomDetailsActivity.class);
            i.putExtra("house_id", houseId);
            i.putExtra("room_id", room.getId());
            startActivity(i);
        }, (room, position) -> {
            RecyclerView.Adapter<?> a = rv.getAdapter();
            if (a instanceof RoomAdapter) {
                showRoomActionsDialog(room, position, rooms, (RoomAdapter) a);
            }
        });
        rv.setAdapter(adapter);

        // Không dùng ivPlus cho thêm phòng; sử dụng FAB riêng
        View fab = findViewById(R.id.fabAddRoom);
        if (fab != null) fab.setOnClickListener(v -> showAddRoomDialog(rooms, adapter));
        findViewById(R.id.ivDevices).setOnClickListener(v ->
                startActivity(new Intent(this, DeviceInventoryActivity.class)));
        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)));
        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        View ivSetting = findViewById(R.id.ivSetting);
        if (ivSetting != null) ivSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showAddRoomDialog(List<Room> rooms, RoomAdapter adapter) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_room_form, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDesc = form.findViewById(R.id.etDescription);
        new AlertDialog.Builder(this)
                .setTitle("Thêm phòng")
                .setView(form)
                .setPositiveButton("Thêm", (d, which) -> {
                    String name = etName.getText() == null ? null : etName.getText().toString();
                    String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                    Room r = SmartRepository.get(this).addRoom(houseId, name, R.drawable.ic_room_generic);
                    if (r != null) {
                        SmartRepository.get(this).updateRoom(houseId, r.getId(), null, desc);
                        // Không thêm thủ công vào danh sách vì repo đã thêm rồi -> chỉ notify
                        adapter.notifyItemInserted(rooms.size() - 1);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditRoomDialog(Room room, int position, List<Room> rooms, RoomAdapter adapter) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_room_form, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDesc = form.findViewById(R.id.etDescription);
        etName.setText(room.getName());
        etDesc.setText(room.getDescription());
        new AlertDialog.Builder(this)
                .setTitle("Sửa phòng")
                .setView(form)
                .setPositiveButton("Lưu", (d, which) -> {
                    String name = etName.getText() == null ? null : etName.getText().toString();
                    String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                    SmartRepository.get(this).updateRoom(houseId, room.getId(), name, desc);
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showRoomActionsDialog(Room room, int position, List<Room> rooms, RoomAdapter adapter) {
        String[] actions = new String[]{"Sửa", "Xóa"};
        new AlertDialog.Builder(this)
                .setTitle(room.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showEditRoomDialog(room, position, rooms, adapter);
                    } else if (which == 1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xóa phòng")
                                .setMessage("Bạn có chắc muốn xóa phòng này?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    if (SmartRepository.get(this).deleteRoom(houseId, room.getId())) {
                                        // Repo đã xoá khỏi danh sách, chỉ cần notify
                                        adapter.notifyItemRemoved(position);
                                    }
                                })
                                .setNegativeButton("Hủy", null)
                                .show();
                    }
                })
                .show();
    }
}