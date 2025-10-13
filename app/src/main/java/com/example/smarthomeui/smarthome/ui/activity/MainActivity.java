package com.example.smarthomeui.smarthome.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomeui.smarthome.utils.UserManager;

/**
 * MainActivity - Điểm khởi đầu của ứng dụng
 * Kiểm tra trạng thái đăng nhập và chuyển hướng tương ứng
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Không cần setContentView vì activity này chỉ để điều hướng
        checkLoginStatus();
    }

    /**
     * Kiểm tra trạng thái đăng nhập và điều hướng
     */
    private void checkLoginStatus() {
        UserManager userManager = new UserManager(this);

        if (userManager.isLoggedIn()) {
            // User đã đăng nhập và token còn hạn
            String userRole = userManager.getUserRole();
            navigateToHome(userRole);
        } else {
            // User chưa đăng nhập hoặc token đã hết hạn
            navigateToLogin();
        }
    }

    /**
     * Chuyển đến màn hình chính dựa trên role
     */
    private void navigateToHome(String userRole) {
        Intent intent;
        if ("admin".equals(userRole)) {
            intent = new Intent(this, AdminDashboardActivity.class);
        } else {
            intent = new Intent(this, HouseListActivity.class);
        }
        startActivity(intent);
        finish();
    }

    /**
     * Chuyển đến màn hình đăng nhập
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
