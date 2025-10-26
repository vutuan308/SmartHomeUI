package com.example.smarthomeui.smarthome.ai_speech_reg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.ParseResult;
import com.example.smarthomeui.smarthome.network.Api;
import com.example.smarthomeui.smarthome.network.ApiClient;
import com.example.smarthomeui.smarthome.network.DeviceControlRequest;
import com.example.smarthomeui.smarthome.network.DeviceControlResponse;
import com.example.smarthomeui.smarthome.ui.activity.DeviceInventoryActivity;
import com.example.smarthomeui.smarthome.ui.activity.HouseListActivity;
import com.example.smarthomeui.smarthome.ui.activity.HouseRoomsActivity;
import com.example.smarthomeui.smarthome.ui.activity.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivitySR extends AppCompatActivity {

    private TextView tvHeard, tvResult;
    private FloatingActionButton fabMic;

    private SpeechRecognizer recognizer;
    private SpeechHelper speechHelper;

    private DeviceRegistry registry;
    private VietnameseCommandParser parser;

    private  ImageView ivHome, ivRooms, ivControl, ivDevices, ivSetting;
    private final ActivityResultLauncher<String> micPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startListening();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_speech_recognition);

        init();
        action();
        navbarAciton();
    }

    private void init(){
        tvHeard = findViewById(R.id.tvHeard);
        tvResult = findViewById(R.id.tvResult);
        fabMic  = findViewById(R.id.fabMic);

        registry = new DeviceRegistry();
        parser   = new VietnameseCommandParser(registry);
        speechHelper = new SpeechHelper(this);

        ivHome    = findViewById(R.id.ivHome);
        ivRooms   = findViewById(R.id.ivRooms);
        ivControl = findViewById(R.id.ivControl);
        ivDevices = findViewById(R.id.ivDevices);
        ivSetting = findViewById(R.id.ivSetting);
    }

    private void action(){
        fabMic.setOnClickListener(v -> requestMicAndStart());

        registry.refreshFromApi(this, 0, 200, new DeviceRegistry.LoadCallback() {
            @Override public void onLoaded(int count) {
                // (tuỳ chọn) thông báo nhẹ
                runOnUiThread(() -> {
                    if (tvResult != null && count >= 0) {
                        tvResult.setText("Đã tải " + count + " thiết bị từ máy chủ.");
                    }
                });
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> {
                    if (tvResult != null) tvResult.setText("Không tải được thiết bị: " + message);
                });
            }
        });
    }

    private void navbarAciton(){
        ivHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitySR.this, HouseListActivity.class);
                startActivity(intent);
            }
        });
        ivRooms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitySR.this, HouseRoomsActivity.class);
                startActivity(intent);
            }
        });
        ivDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitySR.this, DeviceInventoryActivity.class);
                startActivity(intent);
            }
        });
        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivitySR.this, SettingsActivity.class);
            }
        });
    }


    private void requestMicAndStart() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            micPermLauncher.launch(Manifest.permission.RECORD_AUDIO);
        } else {
            startListening();
        }
    }

    private void startListening() {
        stopListening(); // dọn nếu có
        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override public void onReadyForSpeech(Bundle params) { tvHeard.setText("Đang lắng nghe…"); }
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onError(int error) { tvHeard.setText("Mic đang bận"); }
            @Override public void onPartialResults(Bundle partialResults) {
                ArrayList<String> list = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (list != null && !list.isEmpty()) {
                    tvHeard.setText("Bạn nói (tạm): " + list.get(0));
                }
            }
            @Override public void onEvent(int eventType, Bundle params) {}
            @Override public void onResults(Bundle results) {
                ArrayList<String> list = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String text = (list != null && !list.isEmpty()) ? list.get(0) : "";
                tvHeard.setText("Bạn nói: " + text);
                emitParse(text);
            }
        });
        Intent intent = speechHelper.buildIntentVI();
        recognizer.startListening(intent);
    }

    private void stopListening() {
        if (recognizer != null) {
            recognizer.cancel();
            recognizer.destroy();
            recognizer = null;
        }
    }

    private void emitParse(String text) {
        ParseResult r = parser.parse(text);

        // Không liên quan / không nghe được
        boolean unrelated = !r.isDeviceKnown() && !r.isActionKnown() && !r.isValueKnown() && !r.isRoomKnown();
        if (unrelated) {
            tvResult.setText("Không biết hoặc không thể nghe. Vui lòng nói lại rõ hơn.");
            return;
        }

        // Không tìm thấy thiết bị
        if (!r.isDeviceKnown()) {
            tvResult.setText("Không biết hoặc không tìm thấy thiết bị phù hợp.");
            return;
        }

        // Có thiết bị → hiển thị sheet xác nhận
        List<DeviceRegistry.CandidateResult> cands = registry.rankCandidates(
                text, r.isRoomKnown()? r.room : null, 5);

        if (cands.isEmpty()) {
            tvResult.setText("Không biết hoặc không tìm thấy thiết bị phù hợp.");
            return;
        }

        DeviceConfirmSheet sheet = new DeviceConfirmSheet(
                this, cands, r,
                new DeviceConfirmSheet.Callback() {
                    @Override
                    public void onConfirmed(Device d, ParseResult planned) {
                        // Cập nhật lại kết quả đã xác nhận
                        planned.deviceId   = d.getId(); // nếu getId là UUID/chuỗi, ta vẫn dùng chuỗi khi call API (ở dưới)
                        planned.deviceName = d.getName();
                        if (!planned.isRoomKnown()) planned.room = d.getRoom();

                        tvResult.setText("Đang gửi lệnh…");

                        // 1) Build request từ action + device.type + value
                        DeviceControlRequest req = buildVoiceRequestForDevice(d, planned);

                        // 2) Gọi API controlDevice qua Retrofit có sẵn
                        Api api = ApiClient.getClient(MainActivitySR.this).create(Api.class);

                        // LƯU Ý: Api.controlDevice nhận id dạng String, dùng d.getId()
                        api.controlDevice(d.getId(), req).enqueue(new retrofit2.Callback<
                                com.example.smarthomeui.smarthome.network.DeviceControlResponse>() {
                            @Override
                            public void onResponse(retrofit2.Call<DeviceControlResponse> call,
                                                   retrofit2.Response<DeviceControlResponse> resp) {
                                runOnUiThread(() -> {
                                    if (resp.isSuccessful() && resp.body() != null) {
                                        String msg = resp.body().getMessage() != null ? resp.body().getMessage() : "Thành công";
                                        tvResult.setText("Thành công: " + msg);
                                    } else if (resp.code() == 401) {
                                        tvResult.setText("Chưa đăng nhập (401). Hãy cấu hình Bearer token trong ApiClient.");
                                    } else {
                                        tvResult.setText("API lỗi " + resp.code());
                                    }
                                });
                            }
                            @Override
                            public void onFailure(retrofit2.Call<DeviceControlResponse> call, Throwable t) {
                                runOnUiThread(() -> tvResult.setText("Lỗi mạng/API: " + t.getMessage()));
                            }
                        });
                    }

                    @Override public void onCanceled() {
                        tvResult.setText("Bạn đã huỷ.");
                    }
                }
        );
        sheet.show();
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static int percentToRaw(int percent) { return clamp((int)Math.round(percent * 255.0 / 100.0), 0, 255); }
    private static int levelToRaw(int level0_3) { return clamp((int)Math.round(level0_3 * (255.0 / 3.0)), 0, 255); }

    // Lấy current raw nếu có; nếu chưa có thì baseline hợp lý theo loại
    private static int currentOrBaseline(Device d, String typeL) {
        Integer v = d.getValue();
        if (v != null) return clamp(v, 0, 255);
        if (typeL.contains("fan") || typeL.contains("quat")) return levelToRaw(1); // quạt mặc định level 1
        return percentToRaw(50); // light & rgb mặc định 50%
    }

    private DeviceControlRequest buildVoiceRequestForDevice(Device d, ParseResult planned) {
        String typeL = (d.getType() == null ? "" : d.getType().toLowerCase(Locale.ROOT));
        DeviceModels.Action a = planned.action;
        Integer v = planned.value;
        if (v != null && v < 0) v = null; // normalize -1 -> null

        final int STEP_LIGHT = 26;  // ~10%
        final int STEP_FAN       = 85;  // ~1 level

        // ===== LIGHT (param = raw 0..255) =====
        if (typeL.contains("light") && !typeL.contains("rgb")) {
            int cur = currentOrBaseline(d, typeL);
            int out;

            switch (a) {
                case TURN_ON:  out = 255; break;
                case TURN_OFF: out = 0;   break;
                case INCREASE: out = clamp(cur + (v != null ? v : STEP_LIGHT), 0, 255); break;
                case DECREASE: out = clamp(cur - (v != null ? v : STEP_LIGHT), 0, 255); break;
                case SET:
                default:       out = (v != null) ? clamp(v, 0, 255) : cur; break;
            }

            return new DeviceControlRequest("setLedDim", new JsonPrimitive(out));
        }

        // ===== RGB (param = {r,g,b} raw 0..255) =====
        if (typeL.contains("rgb")) {
            int cur = currentOrBaseline(d, typeL);
            int raw;
            switch (a) {
                case TURN_ON:  raw = 255; break;
                case TURN_OFF: raw = 0;   break;
                case INCREASE: raw = clamp(cur + (v != null ? v : STEP_LIGHT), 0, 255); break;
                case DECREASE: raw = clamp(cur - (v != null ? v : STEP_LIGHT), 0, 255); break;
                case SET:
                default:       raw = (v != null) ? clamp(v, 0, 255) : cur; break;
            }
            JsonObject rgb = new JsonObject();
            rgb.addProperty("r", raw);
            rgb.addProperty("g", raw);
            rgb.addProperty("b", raw);
            return new DeviceControlRequest("setRgbColor", rgb);
        }

        // ===== FAN (param = raw 0..255; level 0..3 cũng quy ra 0..255 từ parser) =====
        if (typeL.contains("fan") || typeL.contains("quat")) {
            int cur = currentOrBaseline(d, typeL);
            int out;
            switch (a) {
                case TURN_ON:  out = levelToRaw(3); break;
                case TURN_OFF: out = 0;             break;
                case INCREASE: out = clamp(cur + (v != null ? v : STEP_FAN), 0, 255); break;
                case DECREASE: out = clamp(cur - (v != null ? v : STEP_FAN), 0, 255); break;
                case SET:
                default:       out = (v != null) ? clamp(v, 0, 255) : cur; break;
            }
            return new DeviceControlRequest("setFanSpeed", new JsonPrimitive(out));
        }

        // Fallback
        return new DeviceControlRequest("unknown");
    }


    // Nếu muốn giữ deviceId dạng số cho ParseResult (hiển thị), nhưng API nhận String:
    private Integer tryParseIntSafe(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return -1; }
    }
    private String mapActionToCommand(DeviceModels.Action action) {
        switch (action) {
            case TURN_ON:  return "turn_on";
            case TURN_OFF: return "turn_off";
            case INCREASE: return "increase";
            case DECREASE: return "decrease";
            case SET:      return "set";
            default:       return "unknown";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }
}
