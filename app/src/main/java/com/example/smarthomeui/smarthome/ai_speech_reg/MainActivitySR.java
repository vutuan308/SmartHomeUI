package com.example.smarthomeui.smarthome.ai_speech_reg;

import static com.example.smarthomeui.smarthome.ai_speech_reg.DeviceModels.*;
import com.example.smarthomeui.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
            @Override public void onError(int error) { tvHeard.setText("Lỗi ghi âm: " + error); }
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
        DeviceModels.ParseResult r = parser.parse(text);

        // Lấy top-N ứng viên (ví dụ 5)
        List<DeviceRegistry.CandidateResult> cands =
                registry.rankCandidates(text, r.room, 5);

        if (cands.isEmpty()) {
            tvResult.setText("Không tìm thấy thiết bị phù hợp. Hãy nói rõ tên/ phòng.");
            return;
        }

        // Cập nhật planned device từ ứng viên đầu tiên tạm thời (chỉ để hiển thị)
        r.deviceId = cands.get(0).device.id;
        r.deviceName = cands.get(0).device.name;
        if (r.room == null) r.room = cands.get(0).device.room;

        // Hiển thị bottom sheet xác nhận
        DeviceConfirmSheet sheet = new DeviceConfirmSheet(
                this, cands, r,
                new DeviceConfirmSheet.Callback() {
                    @Override
                    public void onConfirmed(DeviceModels.Device d, DeviceModels.ParseResult planned) {
                        // Gắn lại đúng device đã xác nhận
                        planned.deviceId = d.id;
                        planned.deviceName = d.name;
                        if (planned.room == null) planned.room = d.room;
                        // (Chưa có API) -> log/hiển thị
                        String line = planned.toString();
                        tvResult.setText("XÁC NHẬN! " + line);
                        android.widget.Toast.makeText(MainActivitySR.this,
                                "Thực hiện: " + line, android.widget.Toast.LENGTH_SHORT).show();

                        // TODO: Sau này gọi API điều khiển tại đây
                        // callControlApi(planned);
                    }
                    @Override public void onCanceled() {
                        tvResult.setText("Bạn đã huỷ.");
                    }
                }
        );
        sheet.show();

        // Hiện câu đã nghe
        tvHeard.setText("Bạn nói: " + text);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopListening();
    }
}
