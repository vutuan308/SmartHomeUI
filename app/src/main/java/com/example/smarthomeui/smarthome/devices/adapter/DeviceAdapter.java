package com.example.smarthomeui.smarthome.devices.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.devices.model.Device;

import java.util.List;


public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.VH> {
    public interface OnItemClick { void onClick(Device d); }
    public interface OnToggle { void onToggle(Device d, boolean on); }
    public interface OnDelete { void onDelete(Device d); }


    private final List<Device> data;
    private final OnItemClick click; private final OnToggle toggle; private final OnDelete delete;


    public DeviceAdapter(List<Device> data, OnItemClick click, OnToggle toggle, OnDelete delete) {
        this.data = data; this.click = click; this.toggle = toggle; this.delete = delete;
    }


    static class VH extends RecyclerView.ViewHolder {
        ImageView icon; TextView name, meta; Switch sw;
        VH(@NonNull View v) { super(v);
            icon = v.findViewById(R.id.icon); name = v.findViewById(R.id.name);
            meta = v.findViewById(R.id.meta); sw = v.findViewById(R.id.toggle);
        }
    }


    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        return new VH(LayoutInflater.from(p.getContext()).inflate(R.layout.item_device, p, false));
    }


    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Device d = data.get(pos);
        h.name.setText(d.getName());
        switch (d.getType()) {
            case FAN:
                h.icon.setImageResource(R.drawable.ic_fan);
                h.meta.setText("Cấp: " + d.getFanLevel());
                break;
            case LIGHT_RGB:
                h.icon.setImageResource(R.drawable.ic_rgb);
                h.meta.setText("Sáng: " + d.getBrightness() + "%");
                break;
            default:
                h.icon.setImageResource(R.drawable.ic_light);
                h.meta.setText("Sáng: " + d.getBrightness() + "%");
        }
        h.sw.setOnCheckedChangeListener(null);
        h.sw.setChecked(d.isOn());
        h.sw.setOnCheckedChangeListener((b, on) -> toggle.onToggle(d, on));
        h.itemView.setOnClickListener(v -> click.onClick(d));
        h.itemView.setOnLongClickListener(v -> { delete.onDelete(d); return true; });
    }


    @Override public int getItemCount() { return data.size(); }
}
