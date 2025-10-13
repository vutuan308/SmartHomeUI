package com.example.smarthomeui.smarthome.devices.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.devices.adapter.DeviceAdapter;
import com.example.smarthomeui.smarthome.devices.data.DeviceRepository;
import com.example.smarthomeui.smarthome.devices.model.Device;
import com.example.smarthomeui.smarthome.ui.activity.AllRoomsActivity;
import com.example.smarthomeui.smarthome.ui.activity.HouseListActivity;
import com.example.smarthomeui.smarthome.ui.activity.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class DeviceListActivity extends AppCompatActivity {
    private final List<Device> data = new ArrayList<>();
    private DeviceAdapter adapter;

    private  FloatingActionButton add;
    private RecyclerView rv;
    private ImageView ivHome;
    private ImageView ivRooms;
    private ImageView ivSetting;
    @Override protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_device_list);

        init();

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(data,
                d -> openForm(d.getId()),
                (d, on) -> { d.setOn(on); /* TODO call API */ },
                d -> confirmDelete(d)
        );
        rv.setAdapter(adapter);


        //Chuc nang navbar
        add.setOnClickListener(v -> openForm(null));

        ivHome.setOnClickListener(v -> startActivity(new Intent(this, HouseListActivity.class)));

        ivRooms.setOnClickListener(v -> startActivity(new Intent(this, AllRoomsActivity.class)));

        ivSetting.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void init(){
        rv = findViewById(R.id.recycler);
        add = findViewById(R.id.btnAdd);
        ivHome = findViewById(R.id.ivHome);
        ivRooms = findViewById(R.id.ivRooms);
        ivSetting = findViewById(R.id.ivSetting);
    }


    private void openForm(@Nullable String id) {
        Intent i = new Intent(this, DeviceFormActivity.class);
        if (id != null) i.putExtra(DeviceFormActivity.EXTRA_ID, id);
        startActivity(i);
    }


    private void confirmDelete(Device d) {
        new AlertDialog.Builder(this)
                .setMessage("Xoá thiết bị '" + d.getName() + "'?")
                .setPositiveButton("Xoá", (dlg, w) -> {
                    if (DeviceRepository.get().delete(d.getId())) refresh();
                })
                .setNegativeButton("Huỷ", null)
                .show();
    }


    private void refresh() {
        data.clear();
        data.addAll(DeviceRepository.get().list());
        adapter.notifyDataSetChanged();
    }


    @Override protected void onResume() { super.onResume(); refresh(); }
}
