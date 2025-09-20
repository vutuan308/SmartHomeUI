package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
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

        houseId = getIntent().getStringExtra("house_id");
        House h = SmartRepository.get(this).getHouseById(houseId);
        setTitle(h != null ? h.getName() : "Phòng");
        TextView title = findViewById(R.id.tvHouseTitle);
        if (title != null && h != null) title.setText(h.getName());

        RecyclerView rv = findViewById(R.id.rvRoomsOfHouse);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        List<Room> rooms = SmartRepository.get(this).getRooms(houseId);
        RoomAdapter adapter = new RoomAdapter(rooms, room -> {
            Intent i = new Intent(HouseRoomsActivity.this, RoomDetailsActivity.class);
            i.putExtra("house_id", houseId);
            i.putExtra("room_id", room.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);

        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)));
        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
    }
}