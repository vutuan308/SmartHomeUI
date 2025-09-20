package com.example.smarthomeui.smarthome.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.House;

import java.util.List;

public class HouseAdapter extends RecyclerView.Adapter<HouseAdapter.VH> {

    public interface OnHouseClick { void onClick(House h); }

    private final List<House> data;
    private final OnHouseClick listener;

    public HouseAdapter(List<House> data, OnHouseClick l) {
        this.data = data;
        this.listener = l;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_row, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        House x = data.get(position);
        h.ivIcon.setImageResource(x.getIconRes() != 0 ? x.getIconRes() : R.drawable.ic_house);
        h.tvName.setText(x.getName());
        h.tvCount.setText(x.getRoomCount() + " phòng");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(x);
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvName, tvCount;

        VH(@NonNull View v) {
            super(v);
            ivIcon = v.findViewById(R.id.ivRoomIcon);
            tvName = v.findViewById(R.id.tvRoomName);
            tvCount = v.findViewById(R.id.tvDeviceCount);
        }
    }
}
