package com.example.smarthomeui.smarthome.ui.activity;

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
import com.example.smarthomeui.smarthome.model.Room;
import com.example.smarthomeui.smarthome.network.*;

import java.util.*;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllRoomsActivity extends AppCompatActivity {

    interface Row {}

    static class HeaderRow implements Row {
        final String houseId;
        final String houseName;
        final String houseLocation;
        HeaderRow(String id, String name, @Nullable String location) {
            houseId = id; houseName = name; houseLocation = location;
        }
    }
    static class RoomRow implements Row { final String houseId; final Room room; RoomRow(String hid, Room r){ houseId=hid; room=r; } }

    private final List<Row> rows = new ArrayList<>();
    private final Map<Integer, String> houseLocMap = new HashMap<>(); // houseId -> location

    private RoomsSectionAdapter adapter;
    private View loading;

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_house_rooms);
        setTitle("Danh sách phòng");

        View back = findViewById(R.id.ivBack);
        if (back != null) back.setOnClickListener(v -> onBackPressed());
        TextView title = findViewById(R.id.tvHouseTitle);
        if (title != null) title.setText("Danh sách phòng");

        RecyclerView rv = findViewById(R.id.rvRoomsOfHouse);
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        rv.setLayoutManager(glm);
        adapter = new RoomsSectionAdapter(rows);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int pos) {
                return adapter.getItemViewType(pos) == RoomsSectionAdapter.VT_HEADER ? 2 : 1;
            }
        });
        rv.setAdapter(adapter);

        View ivHome = findViewById(R.id.ivHome);
        if (ivHome != null) ivHome.setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        findViewById(R.id.ivRooms).setSelected(true);
        View ivDevices = findViewById(R.id.ivDevices);
        if (ivDevices != null) ivDevices.setOnClickListener(v ->
                startActivity(new Intent(this, DeviceInventoryActivity.class)));
        View ivSetting = findViewById(R.id.ivSetting);
        if (ivSetting != null) ivSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));

        View fab = findViewById(R.id.fabAddRoom);
        if (fab != null) fab.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setMessage("Tạo phòng mới sẽ gọi POST /api/room (sẽ bổ sung).")
                        .setPositiveButton("OK", null)
                        .show());

        loading = attachSimpleLoading();

        // 1) Lấy danh sách nhà (để có location), 2) rồi lấy rooms grouped
        fetchHouseLocationsThenRooms(0, 200, 0, 50);
    }

    /** Bước 1: GET /api/house -> build map houseId -> location, rồi gọi bước 2 */
    private void fetchHouseLocationsThenRooms(int houseSkip, int houseTake, int roomSkip, int roomTake) {
        showLoading(true);
        Api api = ApiClient.getClient(this).create(Api.class);
        api.getHouses(houseSkip, houseTake).enqueue(new Callback<HouseListWrap>() {
            @Override public void onResponse(Call<HouseListWrap> call, Response<HouseListWrap> resp) {
                if (resp.isSuccessful() && resp.body() != null && resp.body().houses != null) {
                    houseLocMap.clear();
                    for (HouseDto h : resp.body().houses) {
                        houseLocMap.put(h.id, h.location); // có thể null
                    }
                }
                // Dù thành công hay không, vẫn tiếp tục gọi rooms (địa chỉ sẽ là "Địa chỉ" khi null)
                fetchRoomsGrouped(roomSkip, roomTake);
            }
            @Override public void onFailure(Call<HouseListWrap> call, Throwable t) {
                // Không có map -> location sẽ để mặc định
                fetchRoomsGrouped(roomSkip, roomTake);
            }
        });
    }

    /** Bước 2: GET /api/room -> hiển thị theo nhóm nhà, chèn location từ map */
    private void fetchRoomsGrouped(int skip, int take) {
        Api api = ApiClient.getClient(this).create(Api.class);
        api.getRoomsGrouped(skip, take).enqueue(new Callback<RoomsByHouseWrap>() {
            @Override public void onResponse(Call<RoomsByHouseWrap> call, Response<RoomsByHouseWrap> resp) {
                showLoading(false);
                if (!resp.isSuccessful() || resp.body() == null) {
                    toast("Tải danh sách phòng thất bại: " + resp.code());
                    return;
                }
                rows.clear();
                RoomsByHouseWrap data = resp.body();
                if (data.groups != null) {
                    for (RoomsByHouseWrap.Group g : data.groups) {
                        int hidInt = g.houseId;
                        String hid  = String.valueOf(hidInt);
                        String name = safe(g.houseName, "Nhà");
                        // Ưu tiên location từ API group; nếu null thì lấy từ map houses
                        String loc  = safe(nonNull(g.houseLocation, houseLocMap.get(hidInt)), "Địa chỉ");
                        rows.add(new HeaderRow(hid, name, loc));

                        if (g.rooms != null) {
                            for (RoomDto r : g.rooms) {
                                rows.add(new RoomRow(hid, mapToUiRoom(r)));
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override public void onFailure(Call<RoomsByHouseWrap> call, Throwable t) {
                showLoading(false);
                toast("Lỗi mạng: " + (t != null ? t.getMessage() : ""));
            }
        });
    }

    private Room mapToUiRoom(RoomDto r) {
        int icon = mapIcon(r.iconKey, r.type);
        Room room = new Room(String.valueOf(r.id), safe(r.name, "Phòng"), icon);
        room.setDescription(r.description);
        room.setDeviceCount(r.deviceCount != null ? r.deviceCount : 0);
        return room;
    }

    private int mapIcon(String iconKey, String type) {
        String k = (iconKey != null ? iconKey : (type != null ? type : "")).toLowerCase();
        if (k.contains("living"))  return R.drawable.ic_room_living;
        if (k.contains("bed"))     return R.drawable.ic_room_bed;
        if (k.contains("kitchen")) return R.drawable.ic_room_kitchen;
        return R.drawable.ic_room_generic;
    }

    private String nonNull(String a, String b){ return a != null ? a : b; }
    private String safe(String s, String def){ return (s == null || s.trim().isEmpty()) ? def : s.trim(); }

    private View attachSimpleLoading() {
        ViewGroup root = findViewById(android.R.id.content);
        FrameLayout overlay = new FrameLayout(this);
        overlay.setClickable(true);
        overlay.setVisibility(View.GONE);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleLarge);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        overlay.addView(bar, lp);
        root.addView(overlay, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return overlay;
    }
    private void showLoading(boolean show){ if (loading != null) loading.setVisibility(show ? View.VISIBLE : View.GONE); }
    private void toast(String s){ Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    /* ============ Adapter ============ */
    static class RoomsSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        static final int VT_HEADER = 0, VT_ROOM = 1;
        private final List<Row> data;
        RoomsSectionAdapter(List<Row> d){ data=d; }

        @Override public int getItemViewType(int pos) { return (data.get(pos) instanceof HeaderRow) ? VT_HEADER : VT_ROOM; }

        @NonNull @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            LayoutInflater inf = LayoutInflater.from(p.getContext());
            if (vt == VT_HEADER) return new HeaderVH(inf.inflate(R.layout.row_section_header, p, false));
            return new RoomVH(inf.inflate(R.layout.home_row, p, false));
        }

        @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
            if (getItemViewType(pos) == VT_HEADER) {
                HeaderRow hr = (HeaderRow) data.get(pos);
                HeaderVH hv = (HeaderVH) holder;
                hv.tvTitle.setText(hr.houseName);
                if (hv.tvSubtitle != null) hv.tvSubtitle.setText(hr.houseLocation); // <— địa chỉ
                return;
            }
            RoomRow rr = (RoomRow) data.get(pos);
            Room r = rr.room;
            RoomVH vh = (RoomVH) holder;
            vh.ivIcon.setImageResource(r.getIconRes() != 0 ? r.getIconRes() : R.drawable.room);
            vh.tvName.setText(r.getName());
            vh.tvCount.setText(r.getDeviceCount() + " thiết bị");
            vh.itemView.setOnClickListener(v -> {
                Intent i = new Intent(v.getContext(), RoomDetailsActivity.class);
                i.putExtra("house_id", rr.houseId);
                i.putExtra("room_id",  r.getId());
                v.getContext().startActivity(i);
            });
            vh.itemView.setOnLongClickListener(v -> false);
        }

        @Override public int getItemCount() { return data.size(); }

        static class HeaderVH extends RecyclerView.ViewHolder {
            final TextView tvTitle, tvSubtitle;
            HeaderVH(View v){ super(v);
                tvTitle = v.findViewById(R.id.tvSectionTitle);
                tvSubtitle = v.findViewById(R.id.tvSectionSubtitle); // cần có trong XML
            }
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
