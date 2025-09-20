package com.example.smarthomeui.smarthome.components;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.model.Device;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DeviceControlBottomSheet extends BottomSheetDialogFragment {

    public interface OnDeviceChanged { void onChanged(Device d); }

    private static final String ARG_DEVICE = "arg_device";

    private Device device;
    private OnDeviceChanged callback;

    // Factory khởi tạo
    public static DeviceControlBottomSheet newInstance(Device d, OnDeviceChanged cb) {
        DeviceControlBottomSheet f = new DeviceControlBottomSheet();
        Bundle b = new Bundle();
        b.putSerializable(ARG_DEVICE, d); // Device implements Serializable
        f.setArguments(b);
        f.setOnDeviceChanged(cb);
        return f;
    }

    public void setOnDeviceChanged(OnDeviceChanged cb){ this.callback = cb; }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(false);
        if (getArguments() != null) {
            Object obj = getArguments().getSerializable(ARG_DEVICE);
            if (obj instanceof Device) device = (Device) obj;
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View content = LayoutInflater.from(getContext()).inflate(R.layout.dialog_device_control, null, false);
        dialog.setContentView(content);

        if (device == null) { dismiss(); return dialog; }

        TextView tvTitle      = content.findViewById(R.id.tvTitle);
        SwitchCompat swPower  = content.findViewById(R.id.swPower);
        View groupLight       = content.findViewById(R.id.groupLight);
        View groupFan         = content.findViewById(R.id.groupFan);
        SeekBar seekBrightness= content.findViewById(R.id.seekBrightness);
        SeekBar seekSpeed     = content.findViewById(R.id.seekSpeed);
        View btnPickColor     = content.findViewById(R.id.btnPickColor); // có thể là Button hoặc View tròn dùng làm preview
        View btnClose         = content.findViewById(R.id.btnClose);

        // Tiêu đề
        if (tvTitle != null) tvTitle.setText(device.getName());

        // Power
        if (swPower != null) {
            swPower.setOnCheckedChangeListener(null);
            swPower.setChecked(device.isOn());
            swPower.setOnCheckedChangeListener((b, checked) -> {
                device.setOn(checked);
                syncEnabledState(groupLight, groupFan, seekBrightness, seekSpeed, btnPickColor, checked);
                fireChanged();
            });
        }

        // LIGHT
        if (device.isLight()) {
            if (groupLight != null) groupLight.setVisibility(View.VISIBLE);
            if (seekBrightness != null) {
                seekBrightness.setMax(100);
                seekBrightness.setOnSeekBarChangeListener(null);
                seekBrightness.setProgress(device.getBrightness());
                seekBrightness.setOnSeekBarChangeListener(new SimpleSeek(p -> {
                    device.setBrightness(p);
                    fireChanged();
                }));
            }
            if (btnPickColor != null) {
                // tô preview theo màu hiện tại nếu là View
                try { btnPickColor.getBackground().setTint(device.getColor()); } catch (Exception ignore) {}
                btnPickColor.setOnClickListener(v ->
                        AdvancedColorPickerDialog.newInstance(device.getColor(), picked -> {
                            device.setColor(picked);
                            try { btnPickColor.getBackground().setTint(picked); } catch (Exception ignore) {}
                            fireChanged();
                        }).show(getParentFragmentManagerSafe(), "adv_color_picker")
                );
            }
        } else if (groupLight != null) groupLight.setVisibility(View.GONE);

        // FAN
        if (device.isFan()) {
            if (groupFan != null) groupFan.setVisibility(View.VISIBLE);
            if (seekSpeed != null) {
                seekSpeed.setMax(3);
                seekSpeed.setOnSeekBarChangeListener(null);
                seekSpeed.setProgress(device.getSpeed());
                seekSpeed.setOnSeekBarChangeListener(new SimpleSeek(p -> {
                    device.setSpeed(p);
                    fireChanged();
                }));
            }
        } else if (groupFan != null) groupFan.setVisibility(View.GONE);

        // Khóa / mở controls theo power ban đầu
        syncEnabledState(groupLight, groupFan, seekBrightness, seekSpeed, btnPickColor, device.isOn());

        // Đóng
        if (btnClose != null) btnClose.setOnClickListener(v -> dismiss());

        return dialog;
    }

    private void syncEnabledState(@Nullable View groupLight, @Nullable View groupFan,
                                  @Nullable SeekBar seekBrightness, @Nullable SeekBar seekSpeed,
                                  @Nullable View btnPickColor, boolean enabled) {
        // Chỉ disable tương tác; vẫn hiển thị để người dùng hiểu ngữ cảnh
        if (groupLight != null) groupLight.setAlpha(enabled ? 1f : 0.5f);
        if (groupFan   != null) groupFan.setAlpha(enabled ? 1f : 0.5f);
        if (seekBrightness != null) seekBrightness.setEnabled(enabled);
        if (seekSpeed != null) seekSpeed.setEnabled(enabled);
        if (btnPickColor != null) btnPickColor.setEnabled(enabled);
    }

    private void fireChanged() {
        if (callback != null) callback.onChanged(device);
    }

    // Helper rút gọn cho SeekBar
    private static class SimpleSeek implements SeekBar.OnSeekBarChangeListener {
        interface OnChange { void on(int progress); }
        private final OnChange cb;
        SimpleSeek(OnChange cb){ this.cb = cb; }
        @Override public void onProgressChanged(SeekBar sb, int p, boolean fromUser){ if (fromUser) cb.on(p); }
        @Override public void onStartTrackingTouch(SeekBar seekBar) {}
        @Override public void onStopTrackingTouch(SeekBar seekBar) {}
    }

    private FragmentManager getParentFragmentManagerSafe() {
        // Với BottomSheetDialogFragment, getParentFragmentManager() là đúng;
        // phòng trường hợp gọi từ Fragment cha, vẫn an toàn.
        return getParentFragmentManager();
    }
}
