package com.example.smarthomeui.smarthome.ai_speech_reg;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
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

import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.ParseResult;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.example.smarthomeui.smarthome.ui.activity.AllRoomsActivity;
import com.example.smarthomeui.smarthome.ui.activity.DeviceInventoryActivity;
import com.example.smarthomeui.smarthome.ui.activity.HouseListActivity;
import com.example.smarthomeui.smarthome.ui.activity.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivitySR extends AppCompatActivity {

    private TextView tvHeard, tvResult;
    private FloatingActionButton fabMic;

    private SpeechRecognizer recognizer;
    private SpeechHelper speechHelper;

    private DeviceRegistry registry;
    private VietnameseCommandParser parser;

    private final ActivityResultLauncher<String> micPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startListening();
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_speech_recognition);

        tvHeard = findViewById(R.id.tvHeard);
        tvResult = findViewById(R.id.tvResult);
        fabMic  = findViewById(R.id.fabMic);

        registry = new DeviceRegistry();
        parser   = new VietnameseCommandParser(registry);
        speechHelper = new SpeechHelper(this);

        fabMic.setOnClickListener(v -> requestMicAndStart());
        setupBottomNavigation();
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
    private void setupBottomNavigation() {
        findViewById(R.id.ivDevices).setOnClickListener(v ->
                startActivity(new Intent(this, DeviceInventoryActivity.class)));
        findViewById(R.id.ivRooms).setOnClickListener(v ->
                startActivity(new Intent(this, AllRoomsActivity.class)));
        findViewById(R.id.ivControl).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivitySR.class)));
        findViewById(R.id.ivHome).setOnClickListener(v ->
                startActivity(new Intent(this, HouseListActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        View ivSetting = findViewById(R.id.ivSetting);
        if (ivSetting != null) ivSetting.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
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
                        planned.deviceId = d.getId();
                        planned.deviceName = d.getName();
                        if (!planned.isRoomKnown()) planned.room = d.getRoom();

                        // Chưa có API → hiển thị phần “Phản hồi hệ thống”
                        tvResult.setText("ĐÃ XÁC NHẬN: " + planned.toString());
                        // TODO: Khi có API: map action/value và gọi endpoint điều khiển tại đây.
                    }
                    @Override public void onCanceled() {
                        tvResult.setText("Bạn đã huỷ.");
                    }
                }
        );
        sheet.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }
}
