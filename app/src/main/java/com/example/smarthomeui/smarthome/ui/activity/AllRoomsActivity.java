package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.example.smarthomeui.smarthome.model.House;
import com.example.smarthomeui.smarthome.model.Room;
import java.util.*;

public class AllRoomsActivity extends AppCompatActivity {

    interface Row {}
    static class HeaderRow implements Row { final String houseId, houseName; HeaderRow(String id, String name){ houseId=id; houseName=name; } }
    static class RoomRow implements Row { final String houseId; final Room room; RoomRow(String hid, Room r){ houseId=hid; room=r; } }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_rooms);    // <— dùng layout bạn muốn
        setTitle("Danh sách phòng");

        TextView title = findViewById(R.id.tvHouseTitle);
        if (title != null) title.setText("Danh sách phòng");

        RecyclerView rv = findViewById(R.id.rvRoomsOfHouse);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rv.setLayoutManager(glm);

        // Build data: group theo nhà
        List<Row> rows = new ArrayList<>();
        for (House h : SmartRepository.get(this).getHouses()) {
            rows.add(new HeaderRow(h.getId(), h.getName()));
            for (Room r : h.getRooms()) rows.add(new RoomRow(h.getId(), r));
        }

        RoomsSectionAdapter adapter = new RoomsSectionAdapter(rows);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int pos) {
                return adapter.getItemViewType(pos) == RoomsSectionAdapter.VT_HEADER ? 2 : 1;
            }
        });
        rv.setAdapter(adapter);

        // Nav: đánh dấu tab “Phòng” đang active & gắn action
        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        findViewById(R.id.ivRooms).setSelected(true);

            // TODO: dialog Thêm Phòng/Thiết bị nếu muốn

    }

    static class RoomsSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int VT_HEADER = 0, VT_ROOM = 1;
        private final List<Row> data;
        RoomsSectionAdapter(List<Row> d){ data=d; }

        @Override public int getItemViewType(int pos) { return (data.get(pos) instanceof HeaderRow) ? VT_HEADER : VT_ROOM; }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            LayoutInflater inf = LayoutInflater.from(p.getContext());
            if (vt == VT_HEADER) {
                return new HeaderVH(inf.inflate(R.layout.row_section_header, p, false));
            } else {
                return new RoomVH(inf.inflate(R.layout.home_row, p, false));
            }
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder h, int pos) {
            if (getItemViewType(pos) == VT_HEADER) {
                HeaderRow hr = (HeaderRow) data.get(pos);
                ((HeaderVH) h).tv.setText(hr.houseName);
            } else {
                RoomRow rr = (RoomRow) data.get(pos);
                Room r = rr.room;
                RoomVH vh = (RoomVH) h;
                vh.ivIcon.setImageResource(r.getIconRes() != 0 ? r.getIconRes() : R.drawable.room);
                vh.tvName.setText(r.getName());
                vh.tvCount.setText(r.getDeviceCount() + " thiết bị");
                vh.itemView.setOnClickListener(v -> {
                    Intent i = new Intent(v.getContext(), RoomDetailsActivity.class);
                    i.putExtra("house_id", rr.houseId);
                    i.putExtra("room_id",  r.getId());
                    v.getContext().startActivity(i);
                });
            }
        }

        @Override public int getItemCount() { return data.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            final TextView tv; HeaderVH(View v){ super(v); tv=v.findViewById(R.id.tvSectionTitle); }
        }
        static class RoomVH extends RecyclerView.ViewHolder {
            final ImageView ivIcon; final TextView tvName; final TextView tvCount;
            RoomVH(View v){ super(v);
                ivIcon=v.findViewById(R.id.ivRoomIcon);
                tvName=v.findViewById(R.id.tvRoomName);
                tvCount=v.findViewById(R.id.tvDeviceCount);
            }
        }
    }
}
