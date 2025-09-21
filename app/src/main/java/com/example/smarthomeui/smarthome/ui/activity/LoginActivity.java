package com.example.smarthomeui.smarthome.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomeui.R;

/**
 * Smart Home - Login Activity
 * Màn hình đăng nhập chính của ứng dụng
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;

    public static void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Thiết lập giao diện full screen và status bar trong suốt
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        handleIntentData();
    }

    /**
     * Khởi tạo các view components
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
    }

    /**
     * Xử lý dữ liệu từ Intent (auto-fill email từ RegisterActivity)
     */
    private void handleIntentData() {
        // Auto-fill email nếu được truyền từ RegisterActivity
        String email = getIntent().getStringExtra("email");
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
    }

    /**
     * Xử lý sự kiện khi người dùng ấn nút Đăng nhập
     * TODO: Tích hợp API đăng nhập từ backend
     */
    public void onLoginClicked(View view) {
        // Lấy thông tin đăng nhập từ form
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // TODO: Thay thế bằng validation thật và call API đăng nhập
        // Hiện tại cho phép đăng nhập thành công để test UI
        performLogin(email, password);
    }

    /**
     * Thực hiện đăng nhập
     * TODO: Call API đăng nhập thật từ backend
     * @param email Email đăng nhập
     * @param password Mật khẩu đăng nhập
     */
    private void performLogin(String email, String password) {
        // TODO: Validation input
        // - Kiểm tra email không rỗng và đúng định dạng
        // - Kiểm tra password không rỗng

        // TODO: Call API đăng nhập
        // LoginRequest request = new LoginRequest(email, password);
        // apiService.login(request).enqueue(new Callback<LoginResponse>() {
        //     @Override
        //     public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
        //         if (response.isSuccessful() && response.body() != null) {
        //             LoginResponse loginResponse = response.body();
        //             // Lưu token, user info vào SharedPreferences
        //             // Chuyển đến màn hình chính
        //             navigateToHome();
        //         } else {
        //             showError("Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.");
        //         }
        //     }
        //     @Override
        //     public void onFailure(Call<LoginResponse> call, Throwable t) {
        //         showError("Lỗi kết nối. Vui lòng thử lại.");
        //     }
        // });

        // Tạm thời: Đăng nhập thành công để test UI
        Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
        navigateToHome();
    }

    /**
     * Chuyển đến màn hình chính sau khi đăng nhập thành công
     */
    private void navigateToHome() {
        startActivity(new Intent(getApplicationContext(), HouseListActivity.class));
        finish();
    }

    /**
     * Hiển thị thông báo lỗi
     * @param message Nội dung lỗi
     */
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void onRegisterClicked(View view) {
        // Chuyển đến màn đăng ký
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
}
