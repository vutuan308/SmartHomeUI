package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
        }, (house, position) -> {
            RecyclerView.Adapter<?> a = rv.getAdapter();
            if (a instanceof HouseAdapter) {
                showHouseActionsDialog(house, position, houses, (HouseAdapter) a);
            }
        });
        rv.setAdapter(adapter);

        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)));

        // Không dùng ivPlus; dùng FAB riêng để thêm nhà
        View fab = findViewById(R.id.fabAddHouse);
        if (fab != null) fab.setOnClickListener(v -> showAddHouseDialog(houses, adapter));
        findViewById(R.id.ivDevices).setOnClickListener(v ->
                startActivity(new Intent(this, DeviceInventoryActivity.class)));

        // Nút cài đặt
        View ivSetting = findViewById(R.id.ivSetting);
        if (ivSetting != null) ivSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    private void showAddHouseDialog(List<House> houses, HouseAdapter adapter) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_house_form, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDesc = form.findViewById(R.id.etDescription);
        new AlertDialog.Builder(this)
                .setTitle("Thêm nhà")
                .setView(form)
                .setPositiveButton("Thêm", (d, which) -> {
                    String name = etName.getText() == null ? null : etName.getText().toString();
                    String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                    House h = SmartRepository.get(this).addHouse(name);
                    SmartRepository.get(this).updateHouse(h.getId(), null, desc);
                    houses.add(h);
                    adapter.notifyItemInserted(houses.size() - 1);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showEditHouseDialog(House house, int position, List<House> houses, HouseAdapter adapter) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_house_form, null, false);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDesc = form.findViewById(R.id.etDescription);
        etName.setText(house.getName());
        etDesc.setText(house.getDescription());

        new AlertDialog.Builder(this)
                .setTitle("Sửa nhà")
                .setView(form)
                .setPositiveButton("Lưu", (d, which) -> {
                    String name = etName.getText() == null ? null : etName.getText().toString();
                    String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                    SmartRepository.get(this).updateHouse(house.getId(), name, desc);
                    adapter.notifyItemChanged(position);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void showHouseActionsDialog(House house, int position, List<House> houses, HouseAdapter adapter) {
        String[] actions = new String[]{"Sửa", "Xóa"};
        new AlertDialog.Builder(this)
                .setTitle(house.getName())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        showEditHouseDialog(house, position, houses, adapter);
                    } else if (which == 1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Xóa nhà")
                                .setMessage("Bạn có chắc muốn xóa nhà này?")
                                .setPositiveButton("Xóa", (d, w) -> {
                                    if (SmartRepository.get(this).deleteHouse(house.getId())) {
                                        houses.remove(position);
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