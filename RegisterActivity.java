package com.example.tlu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Realtime Database với URL đầy đủ
        databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users");

        ImageButton backButton = findViewById(R.id.back_button);
        TextInputEditText usernameInput = findViewById(R.id.username_input);
        TextInputEditText studentIdInput = findViewById(R.id.student_id_input);
        TextInputEditText emailInput = findViewById(R.id.email_input);
        TextInputEditText passwordInput = findViewById(R.id.password_input);
        TextInputEditText confirmPasswordInput = findViewById(R.id.confirm_password_input);
        TextInputEditText phoneInput = findViewById(R.id.phone_input);
        Button registerButton = findViewById(R.id.register_button);
        TextView loginText = findViewById(R.id.login_text);

        backButton.setOnClickListener(v -> finish());

        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String studentId = studentIdInput.getText().toString().trim();
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();
            String phone = phoneInput.getText().toString().trim();

            if (username.isEmpty()) {
                usernameInput.setError("Vui lòng nhập tên người dùng");
                return;
            }
            if (studentId.isEmpty()) {
                studentIdInput.setError("Vui lòng nhập mã sinh viên");
                return;
            }
            if (email.isEmpty()) {
                emailInput.setError("Vui lòng nhập email");
                return;
            }
            if (password.isEmpty()) {
                passwordInput.setError("Vui lòng nhập mật khẩu");
                return;
            }
            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.setError("Vui lòng nhập lại mật khẩu");
                return;
            }
            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Mật khẩu không khớp");
                return;
            }
            if (phone.isEmpty()) {
                phoneInput.setError("Vui lòng nhập số điện thoại");
                return;
            }

            String userId = databaseReference.push().getKey();
            User user = new User(username, studentId, email, password, phone);

            databaseReference.child(userId).setValue(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainScreenActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RegisterError", "Lỗi đăng ký: " + e.getMessage());
                        Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        loginText.setOnClickListener(v -> finish());
    }

    public static class User {
        public String username, studentId, email, password, phone;

        public User() {
            // Constructor mặc định cần cho Firebase
        }

        public User(String username, String studentId, String email, String password, String phone) {
            this.username = username;
            this.studentId = studentId;
            this.email = email;
            this.password = password;
            this.phone = phone;
        }
    }
}