package com.example.smarthomeui.smarthome.adapter;

import android.view.*;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.List;

public class SingleRoomAdapter extends RecyclerView.Adapter<SingleRoomAdapter.VH> {
    private final List<Device> data;

    public SingleRoomAdapter(List<Device> data) { this.data = data; }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.single_room_row, p, false);
        return new VH(view);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        Device d = data.get(pos);
        h.name.setText(d.getName());
        h.type.setText(d.getType());
        h.toggle.setChecked(d.isOn());

        int icon = R.drawable.ic_device_other;
        if ("Light".equalsIgnoreCase(d.getType())) icon = R.drawable.ic_device_light;
        else if ("Fan".equalsIgnoreCase(d.getType())) icon = R.drawable.ic_device_fan;
        else if ("AC".equalsIgnoreCase(d.getType())) icon = R.drawable.ic_device_ac;
        h.icon.setImageResource(icon);

        h.toggle.setOnCheckedChangeListener((b, checked) -> d.setOn(checked));
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView icon; TextView name; TextView type; MaterialSwitch toggle;
        VH(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivDeviceIcon);
            name = itemView.findViewById(R.id.tvDeviceName);
            type = itemView.findViewById(R.id.tvDeviceType);
            toggle = itemView.findViewById(R.id.swDeviceOnOff);
        }
    }
}
