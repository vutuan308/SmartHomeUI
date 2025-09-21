package com.example.smarthomeui.smarthome.devices.ui;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.devices.adapter.DeviceAdapter;
import com.example.smarthomeui.smarthome.devices.data.DeviceRepository;
import com.example.smarthomeui.smarthome.devices.model.Device;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class DeviceListActivity extends AppCompatActivity {
    private final List<Device> data = new ArrayList<>();
    private DeviceAdapter adapter;


    @Override protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_device_list);


        RecyclerView rv = findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DeviceAdapter(data,
                d -> openForm(d.getId()),
                (d, on) -> { d.setOn(on); /* TODO call API */ },
                d -> confirmDelete(d)
        );
        rv.setAdapter(adapter);


        FloatingActionButton add = findViewById(R.id.btnAdd);
        add.setOnClickListener(v -> openForm(null));
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
