package com.example.smarthomeui.smarthome.ai_speech_reg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.ai_speech_reg.DeviceController;
import com.example.smarthomeui.smarthome.ai_speech_reg.ApiFactory;
import com.example.smarthomeui.smarthome.ai_speech_reg.DeviceApi;
import com.example.smarthomeui.smarthome.ai_speech_reg.Command;
import com.example.smarthomeui.smarthome.ai_speech_reg.SpeechHelper;
import com.example.smarthomeui.smarthome.ai_speech_reg.VietnameseCommandParser;


public class MainActivitySR extends AppCompatActivity {


    //Chỉnh lại cho đúng môi trường test của bạn
    private static final String BASE_URL = "http://10.0.2.2:5149/"; // Emulator → PC
    private static final String TOKEN = "REPLACE_ME"; // chỉ phần token, KHÔNG có chữ Bearer


    private SpeechHelper speech;
    private VietnameseCommandParser parser;
    private DeviceController controller;


    private TextView tvStatus, tvPartial, tvFinal;


    private final ActivityResultLauncher<String> micPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) Toast.makeText(this, "Cần quyền micro", Toast.LENGTH_SHORT).show();
            });


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ai_speech_recognition);


        tvStatus = findViewById(R.id.tvStatus);
        tvPartial = findViewById(R.id.tvPartial);
        tvFinal = findViewById(R.id.tvFinal);
        Button btnMic = findViewById(R.id.btnMic);


        ensureMicPermission();


        speech = new SpeechHelper(this);
        parser = new VietnameseCommandParser();


        DeviceApi api = ApiFactory.create(BASE_URL, TOKEN);
        controller = new DeviceController(api);


        btnMic.setOnClickListener(v -> {
            tvStatus.setText("Đang nghe...");
            speech.start(new SpeechHelper.Listener() {
                @Override
                public void onPartial(String text) {
                    tvPartial.setText(text);
                }

                @Override
                public void onFinal(String text) {
                    tvFinal.setText(text);
                    handleCommand(text);
                }

                @Override
                public void onError(String message) {
                    tvStatus.setText(message);
                }
            });
        });
    }

    private void handleCommand(String spoken) {
        Command c = parser.parse(spoken);
        if (c == null) {
            toast("Không hiểu lệnh 😅");
            return;
        }


// Demo: map nhanh alias → deviceId. Bạn thay bằng map thực từ API của bạn.
        int deviceId = mapAliasToId(c.device, c.location);
        if (deviceId <= 0) {
            toast("Chưa biết deviceId cho: " + c.device + " - " + c.location);
            return;
        }


        tvStatus.setText("Thực hiện: " + c);
        switch (c.intent) {
            case ON:
                controller.turnOn(deviceId, ok -> runOnUiThread(() -> toast(ok ? "Bật OK" : "Bật lỗi")));
                break;
            case OFF:
                controller.turnOff(deviceId, ok -> runOnUiThread(() -> toast(ok ? "Tắt OK" : "Tắt lỗi")));
                break;
            case SET_BRIGHTNESS:
                int v = c.percentOrValue == null ? 50 : c.percentOrValue;
                controller.setBrightness(deviceId, v, ok -> runOnUiThread(() -> toast(ok ? "Sáng " + v + "% OK" : "Độ sáng lỗi")));
                break;
            default:
                toast("Intent chưa hỗ trợ: " + c.intent);
        }
    }


    private int mapAliasToId(String device, String location) {
        // Ví dụ cứng: đèn phòng khách → id=1
        if ("light".equals(device) && "living_room".equals(location)) return 1;
        // TODO: gọi API /api/device để lấy thật danh sách và map theo tên/phòng → id
        return -1;
    }

    private void ensureMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            micPermission.launch(Manifest.permission.RECORD_AUDIO);
        }
    }


    private void toast(String m) {
        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speech != null) speech.destroy();
    }
}
