package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.RoomAdapter;
import com.example.smarthomeui.smarthome.data.RoomRepository;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // chính là layout bạn gửi
        findViewById(R.id.ivHome).setSelected(true);
        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        List<Room> rooms = RoomRepository.get(this).getRooms();
        RoomAdapter adapter = new RoomAdapter(rooms, room -> {
            Intent i = new Intent(MainActivity.this, RoomDetailsActivity.class);
            i.putExtra("room_id", room.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        // (tuỳ chọn) Nút cộng giữa thanh đáy -> có thể mở dialog "Thêm phòng" sau này
        ImageView plus = findViewById(R.id.ivPlus);
        if (plus != null) {
            plus.setOnClickListener(v -> {
                // TODO: mở dialog thêm Phòng/Thiết bị tùy ý bạn
                // Ví dụ test:
                // Toast.makeText(this, "Plus clicked", Toast.LENGTH_SHORT).show();
            });
        }

    }
}
