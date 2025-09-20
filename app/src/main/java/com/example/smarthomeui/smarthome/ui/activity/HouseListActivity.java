package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.adapter.HouseAdapter;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.House;
import java.util.List;

public class HouseListActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // dùng layout clone từ activity_main

        RecyclerView rv = findViewById(R.id.recycler_view);
        rv.setLayoutManager(new GridLayoutManager(this, 2));

        List<House> houses = SmartRepository.get(this).getHouses();
        HouseAdapter adapter = new HouseAdapter(houses, house -> {
            Intent i = new Intent(HouseListActivity.this, HouseRoomsActivity.class);
            i.putExtra("house_id", house.getId());
            startActivity(i);
        });
        rv.setAdapter(adapter);
        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)));
    }
}