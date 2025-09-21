package com.example.smarthomeui.smarthome.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Room;

import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.VH> {

    public interface OnRoomClick { void onClick(Room r); }
    public interface OnRoomLongClick { void onLongClick(Room r, int position); }

    private final List<Room> data;
    private final OnRoomClick listener;
    private final OnRoomLongClick longListener;

    public RoomAdapter(List<Room> data, OnRoomClick l) {
        this(data, l, null);
    }
    public RoomAdapter(List<Room> data, OnRoomClick l, OnRoomLongClick ll) {
        this.data = data;
        this.listener = l;
        this.longListener = ll;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_row, parent, false); // dùng chung item
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Room r = data.get(position);
        h.ivIcon.setImageResource(r.getIconRes() != 0 ? r.getIconRes() : R.drawable.ic_room_bed);
        h.tvName.setText(r.getName());
        h.tvCount.setText(r.getDeviceCount() + " thiết bị");

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(r);
        });
        h.itemView.setOnLongClickListener(v -> {
            if (longListener != null) {
                int pos = h.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) longListener.onLongClick(r, pos);
                return true;
            }
            return false;
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
