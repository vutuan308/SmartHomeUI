package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.RoomAdapter;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.List;

/**
 * Smart Home - Main Activity
 * Màn hình chính hiển thị tất cả phòng từ mọi nhà
 * Note: Hiện tại launcher activity là HouseListActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRoomsList();
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        // Đánh dấu tab Home là active
        findViewById(R.id.ivHome).setSelected(true);
    }

    /**
     * Thiết lập danh sách phòng từ tất cả các nhà
     */
    private void setupRoomsList() {
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        // Lấy tất cả phòng từ SmartRepository thay vì RoomRepository
        List<Room> rooms = SmartRepository.get(this).getAllRooms();
        RoomAdapter adapter = new RoomAdapter(rooms, room -> {
            // TODO: Cần truyền thêm house_id khi navigate đến RoomDetails
            Intent i = new Intent(MainActivity.this, RoomDetailsActivity.class);
            i.putExtra("room_id", room.getId());
            // i.putExtra("house_id", houseId); // Cần tìm cách lấy house_id
            startActivity(i);
        });
        rv.setAdapter(adapter);
    }
}
