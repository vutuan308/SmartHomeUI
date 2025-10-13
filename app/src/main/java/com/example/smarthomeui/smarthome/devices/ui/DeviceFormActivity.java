package com.example.smarthomeui.smarthome.devices.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.devices.data.DeviceRepository;
import com.example.smarthomeui.smarthome.devices.model.Device;

import java.util.UUID;

public class DeviceFormActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "id";

    private EditText etName, etToken;
    private Spinner spType;
    private Switch swOn;

    // LIGHT
    private View grpLight;
    private SeekBar sbBrightness;

    // FAN (level 1..3)
    private View grpFan;
    private RadioGroup rgFanLevel;

    // RGB
    private View grpRgb;
    private SeekBar sbBrightnessRgb, sbR, sbG, sbB;
    private View colorPreview;

    private Device editing;

    @Override
    protected void onCreate(@Nullable Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_device_form);

        bindViews();
        setupTypeSpinner();

        String id = getIntent().getStringExtra(EXTRA_ID);
        if (id != null) {
            editing = DeviceRepository.get().find(id);
            setTitle("Sửa thiết bị");
            fill(editing);
        } else {
            setTitle("Thêm thiết bị");
        }

        findViewById(R.id.btnSave).setOnClickListener(v -> save());

        findViewById(R.id.btnCancel).setOnClickListener(v -> {
            Toast.makeText(this, "Đã hủy", Toast.LENGTH_SHORT).show();
            finish(); // quay về màn trước
        });
    }

    private void bindViews() {
        etName = findViewById(R.id.etName);
        etToken = findViewById(R.id.etToken);
        spType = findViewById(R.id.spType);
        swOn = findViewById(R.id.swOn);

        grpLight = findViewById(R.id.grpLight);
        sbBrightness = findViewById(R.id.sbBrightness);

        grpFan = findViewById(R.id.grpFan);
        rgFanLevel = findViewById(R.id.rgFanLevel);

        grpRgb = findViewById(R.id.grpRgb);
        sbBrightnessRgb = findViewById(R.id.sbBrightnessRgb);
        sbR = findViewById(R.id.sbR);
        sbG = findViewById(R.id.sbG);
        sbB = findViewById(R.id.sbB);
        colorPreview = findViewById(R.id.colorPreview);

        SeekBar.OnSeekBarChangeListener l = new SeekListener();
        sbBrightnessRgb.setOnSeekBarChangeListener(l);
        sbR.setOnSeekBarChangeListener(l);
        sbG.setOnSeekBarChangeListener(l);
        sbB.setOnSeekBarChangeListener(l);
    }

    private void setupTypeSpinner() {
        ArrayAdapter<String> ad = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Đèn thường", "Đèn RGB", "Quạt"} // 0: LIGHT, 1: RGB, 2: FAN
        );
        spType.setAdapter(ad);
        spType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int p, long id) {
                grpLight.setVisibility(p == 0 ? View.VISIBLE : View.GONE);
                grpRgb.setVisibility(p == 1 ? View.VISIBLE : View.GONE);
                grpFan.setVisibility(p == 2 ? View.VISIBLE : View.GONE);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) { }
        });
    }

    private void fill(Device d) {
        if (d == null) return;
        etName.setText(d.getName());
        etToken.setText(d.getDeviceToken());
        swOn.setChecked(d.isOn());

        if (d.getType() == Device.Type.LIGHT) {
            spType.setSelection(0);
            sbBrightness.setProgress(d.getBrightness());
        } else if (d.getType() == Device.Type.LIGHT_RGB) {
            spType.setSelection(1);
            sbBrightnessRgb.setProgress(d.getBrightness());
            sbR.setProgress(Color.red(d.getColorArgb()));
            sbG.setProgress(Color.green(d.getColorArgb()));
            sbB.setProgress(Color.blue(d.getColorArgb()));
            updatePreview();
        } else { // FAN
            spType.setSelection(2);
            int id = d.getFanLevel() == 2 ? R.id.rb2 :
                    d.getFanLevel() == 3 ? R.id.rb3 : R.id.rb1;
            rgFanLevel.check(id);
        }
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String token = etToken.getText().toString().trim();
        if (name.isEmpty()) { etName.setError("Nhập tên thiết bị"); return; }
        if (token.isEmpty()) { etToken.setError("Nhập device token"); return; }
        boolean on = swOn.isChecked();
        int p = spType.getSelectedItemPosition();

        if (editing == null) {
            Device.Type type = p == 0 ? Device.Type.LIGHT : p == 1 ? Device.Type.LIGHT_RGB : Device.Type.FAN;
            Device d = new Device(UUID.randomUUID().toString(), name, type, token);
            d.setOn(on);

            if (type == Device.Type.LIGHT) {
                d.setBrightness(sbBrightness.getProgress());
            } else if (type == Device.Type.LIGHT_RGB) {
                d.setBrightness(sbBrightnessRgb.getProgress());
                d.setColorArgb(Color.rgb(sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            } else { // FAN
                int level = rgFanLevel.getCheckedRadioButtonId() == R.id.rb2 ? 2 :
                        rgFanLevel.getCheckedRadioButtonId() == R.id.rb3 ? 3 : 1;
                d.setFanLevel(level);
            }
            DeviceRepository.get().add(d);
        } else {
            editing.setName(name);
            editing.setDeviceToken(token);
            editing.setOn(on);

            if (editing.getType() == Device.Type.LIGHT) {
                editing.setBrightness(sbBrightness.getProgress());
            } else if (editing.getType() == Device.Type.LIGHT_RGB) {
                editing.setBrightness(sbBrightnessRgb.getProgress());
                editing.setColorArgb(Color.rgb(sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
            } else { // FAN
                int level = rgFanLevel.getCheckedRadioButtonId() == R.id.rb2 ? 2 :
                        rgFanLevel.getCheckedRadioButtonId() == R.id.rb3 ? 3 : 1;
                editing.setFanLevel(level);
            }
        }
        Toast.makeText(this, "Đã lưu", Toast.LENGTH_SHORT).show();
        finish();
    }

    private class SeekListener implements SeekBar.OnSeekBarChangeListener {
        @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { updatePreview(); }
        @Override public void onStartTrackingTouch(SeekBar seekBar) { }
        @Override public void onStopTrackingTouch(SeekBar seekBar) { }
    }

    private void updatePreview() {
        if (colorPreview == null) return;
        colorPreview.setBackgroundColor(Color.rgb(sbR.getProgress(), sbG.getProgress(), sbB.getProgress()));
    }
}
