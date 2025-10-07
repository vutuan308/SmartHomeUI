package com.example.smarthomeui.smarthome.network;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Gói dữ liệu trả về từ GET /api/room: nhóm phòng theo nhà (dạng .NET có $values) */
public class RoomsByHouseWrap {
    public List<Group> groups = new ArrayList<>();
    public Integer total;
    public Integer skip;
    public Integer take;

    public static class Group {
        public int houseId;
        public String houseName;
        public String houseLocation;
        public List<RoomDto> rooms = new ArrayList<>();
    }

    public static class Deserializer implements JsonDeserializer<RoomsByHouseWrap> {
        @Override
        public RoomsByHouseWrap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx)
                throws JsonParseException {
            RoomsByHouseWrap out = new RoomsByHouseWrap();
            if (json == null || !json.isJsonObject()) return out;

            JsonObject root = json.getAsJsonObject();
            if (root.has("total")) out.total = safeInt(root.get("total"));
            if (root.has("skip"))  out.skip  = safeInt(root.get("skip"));
            if (root.has("take"))  out.take  = safeInt(root.get("take"));

            if (root.has("houses") && root.get("houses").isJsonObject()) {
                JsonObject housesObj = root.getAsJsonObject("houses");
                if (housesObj.has("$values") && housesObj.get("$values").isJsonArray()) {
                    for (JsonElement grpEl : housesObj.getAsJsonArray("$values")) {
                        if (!grpEl.isJsonObject()) continue;
                        JsonObject grpObj = grpEl.getAsJsonObject();

                        Group g = new Group();

                        // house info
                        if (grpObj.has("house") && grpObj.get("house").isJsonObject()) {
                            JsonObject h = grpObj.getAsJsonObject("house");
                            g.houseId = h.has("id") && h.get("id").isJsonPrimitive() ? h.get("id").getAsInt() : 0;
                            g.houseName = h.has("name") && h.get("name").isJsonPrimitive() ? h.get("name").getAsString() : "Nhà";
                        }

                        // rooms list
                        if (grpObj.has("rooms") && grpObj.get("rooms").isJsonObject()) {
                            JsonObject roomsObj = grpObj.getAsJsonObject("rooms");
                            if (roomsObj.has("$values") && roomsObj.get("$values").isJsonArray()) {
                                for (JsonElement rEl : roomsObj.getAsJsonArray("$values")) {
                                    if (!rEl.isJsonObject()) continue;
                                    JsonObject r = rEl.getAsJsonObject();
                                    RoomDto dto = new RoomDto();
                                    dto.id   = r.has("id")   && r.get("id").isJsonPrimitive()   ? r.get("id").getAsInt()   : 0;
                                    dto.name = r.has("name") && r.get("name").isJsonPrimitive() ? r.get("name").getAsString() : "Phòng";
                                    if (r.has("type") && r.get("type").isJsonPrimitive()) dto.type = r.get("type").getAsString();
                                    if (r.has("iconKey") && r.get("iconKey").isJsonPrimitive()) dto.iconKey = r.get("iconKey").getAsString();
                                    if (r.has("description") && r.get("description").isJsonPrimitive()) dto.description = r.get("description").getAsString();
                                    if (r.has("deviceCount") && r.get("deviceCount").isJsonPrimitive()) dto.deviceCount = r.get("deviceCount").getAsInt();
                                    g.rooms.add(dto);
                                }
                            }
                        }

                        out.groups.add(g);
                    }
                }
            }
            return out;
        }

        private static Integer safeInt(JsonElement e) {
            return (e != null && e.isJsonPrimitive()) ? e.getAsInt() : null;
        }
    }
}
