package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomeui.R;
import com.example.smarthomeui.smarthome.data.SmartRepository;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsActivity extends AppCompatActivity {
    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        View cardSecurity = findViewById(R.id.cardSecurity);
        cardSecurity.setOnClickListener(v -> showSecurityOptions());
        setTitle("Cài đặt");
        View back = findViewById(R.id.ivBack);
        if (back != null) back.setOnClickListener(v -> onBackPressed());

        // Demo dữ liệu người dùng
        ((TextView) findViewById(R.id.tvUserName)).setText("Người dùng demo");
        ((TextView) findViewById(R.id.tvUserEmail)).setText("demo@example.com");

        int deviceCount = SmartRepository.get(this).getAllDevices().size();
        ((TextView) findViewById(R.id.tvDevicesCount)).setText(deviceCount + " thiết bị");

        View llLogout = findViewById(R.id.llLogout);
        llLogout.setOnClickListener(v -> {
            // Chuyển về LoginActivity và xóa toàn bộ stack (thoát nick)
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void showSecurityOptions() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Bảo mật & quyền riêng tư")
                .setItems(new CharSequence[]{"Cập nhật thông tin", "Đổi mật khẩu"}, (d, which) -> {
                    if (which == 0) {
                        // Mở màn cập nhật thông tin
                        startActivity(new Intent(this, UpdateAccountActivity.class));
                    } else {
                        // Mở màn đổi mật khẩu
                        startActivity(new Intent(this, ChangePasswordActivity.class));
                    }
                })
                .show();
    }

}
