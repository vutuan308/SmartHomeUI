package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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
        View back = findViewById(R.id.ivBack);
        if (back != null) back.setOnClickListener(v -> onBackPressed());

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

        // Nav: tab
        findViewById(R.id.ivDevices).setOnClickListener(v ->
                startActivity(new Intent(this, DeviceInventoryActivity.class)));
        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        findViewById(R.id.ivRooms).setSelected(true);
        View ivSetting = findViewById(R.id.ivSetting);
        if (ivSetting != null) ivSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        // FAB thêm phòng trong danh sách phòng tổng hợp
        View fab = findViewById(R.id.fabAddRoom);
        if (fab != null) fab.setOnClickListener(v -> showAddRoomInAllDialog(adapter));

            // TODO: dialog Thêm Phòng/Thiết bị nếu muốn

    }

    private void showAddRoomInAllDialog(RoomsSectionAdapter adapter) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_room_add_all, null, false);
        Spinner spHouse = form.findViewById(R.id.spHouse);
        EditText etName = form.findViewById(R.id.etName);
        EditText etDesc = form.findViewById(R.id.etDescription);

        List<House> houses = SmartRepository.get(this).getHouses();
        List<String> names = new ArrayList<>();
        for (House h : houses) names.add(h.getName());
        ArrayAdapter<String> spAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, names);
        spHouse.setAdapter(spAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Thêm phòng")
                .setView(form)
                .setPositiveButton("Thêm", (d, w) -> {
                    int idx = spHouse.getSelectedItemPosition();
                    if (idx < 0 || idx >= houses.size()) return;
                    House h = houses.get(idx);
                    String name = etName.getText() == null ? null : etName.getText().toString();
                    String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                    Room r = SmartRepository.get(this).addRoom(h.getId(), name, R.drawable.ic_room_generic);
                    if (r != null) {
                        SmartRepository.get(this).updateRoom(h.getId(), r.getId(), null, desc);
                        adapter.addRoomRow(h.getId(), h.getName(), r);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
                vh.itemView.setOnLongClickListener(v -> {
                    showRoomActionsDialog(vh, rr);
                    return true;
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

        // Thêm một phòng vào đúng section theo houseId. Nếu chưa có header, tạo mới cuối danh sách.
        void addRoomRow(String houseId, String houseName, Room room) {
            int headerIndex = -1;
            int insertAfter = -1;
            for (int i = 0; i < data.size(); i++) {
                Row row = data.get(i);
                if (row instanceof HeaderRow && ((HeaderRow) row).houseId.equals(houseId)) {
                    headerIndex = i;
                    insertAfter = i;
                } else if (row instanceof RoomRow && ((RoomRow) row).houseId.equals(houseId)) {
                    insertAfter = i;
                }
            }
            if (headerIndex == -1) {
                int start = data.size();
                data.add(new HeaderRow(houseId, houseName != null ? houseName : "Nhà"));
                data.add(new RoomRow(houseId, room));
                notifyItemRangeInserted(start, 2);
            } else {
                int insertPos = insertAfter + 1;
                data.add(insertPos, new RoomRow(houseId, room));
                notifyItemInserted(insertPos);
            }
        }

        private void showRoomActionsDialog(RoomVH vh, RoomRow rr) {
            final int adapterPos = vh.getBindingAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            final Context ctx = vh.itemView.getContext();
            String[] actions = new String[]{"Sửa", "Xóa"};
            new AlertDialog.Builder(ctx)
                    .setTitle(rr.room.getName())
                    .setItems(actions, (dialog, which) -> {
                        if (which == 0) {
                            showEditRoomDialog(vh, rr);
                        } else if (which == 1) {
                            new AlertDialog.Builder(ctx)
                                    .setTitle("Xóa phòng")
                                    .setMessage("Bạn có chắc muốn xóa phòng này?")
                                    .setPositiveButton("Xóa", (d, w) -> {
                                        boolean ok = SmartRepository.get(ctx).deleteRoom(rr.houseId, rr.room.getId());
                                        if (ok) {
                                            int pos = vh.getBindingAdapterPosition();
                                            if (pos != RecyclerView.NO_POSITION) {
                                                data.remove(pos);
                                                notifyItemRemoved(pos);
                                                // Nếu nhà này không còn phòng nào trong danh sách, xoá luôn header
                                                maybeRemoveHeaderForHouse(rr.houseId);
                                            }
                                        }
                                    })
                                    .setNegativeButton("Hủy", null)
                                    .show();
                        }
                    })
                    .show();
        }

        private void showEditRoomDialog(RoomVH vh, RoomRow rr) {
            final Context ctx = vh.itemView.getContext();
            View form = LayoutInflater.from(ctx).inflate(R.layout.dialog_room_form, null, false);
            EditText etName = form.findViewById(R.id.etName);
            EditText etDesc = form.findViewById(R.id.etDescription);
            etName.setText(rr.room.getName());
            etDesc.setText(rr.room.getDescription());
            new AlertDialog.Builder(ctx)
                    .setTitle("Sửa phòng")
                    .setView(form)
                    .setPositiveButton("Lưu", (d, w) -> {
                        String name = etName.getText() == null ? null : etName.getText().toString();
                        String desc = etDesc.getText() == null ? null : etDesc.getText().toString();
                        SmartRepository.get(ctx).updateRoom(rr.houseId, rr.room.getId(), name, desc);
                        int pos = vh.getBindingAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) notifyItemChanged(pos);
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        }

        private void maybeRemoveHeaderForHouse(String houseId) {
            boolean hasAnyRoom = false;
            for (Row row : data) {
                if (row instanceof RoomRow && ((RoomRow) row).houseId.equals(houseId)) { hasAnyRoom = true; break; }
            }
            if (!hasAnyRoom) {
                // tìm header tương ứng và xoá
                for (int i = 0; i < data.size(); i++) {
                    Row row = data.get(i);
                    if (row instanceof HeaderRow && ((HeaderRow) row).houseId.equals(houseId)) {
                        data.remove(i);
                        notifyItemRemoved(i);
                        break;
                    }
                }
            }
        }
    }
}
