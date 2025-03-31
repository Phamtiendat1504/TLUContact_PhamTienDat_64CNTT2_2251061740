package com.example.tlu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileNameTextView, profileEmailTextView, profileStudentIdTextView, profilePhoneTextView;
    private MaterialButton btnEditProfile, btnLogout;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ View
        profileNameTextView = findViewById(R.id.profileNameTextView);
        profileEmailTextView = findViewById(R.id.profileEmailTextView);
        profileStudentIdTextView = findViewById(R.id.profileStudentIdTextView);
        profilePhoneTextView = findViewById(R.id.profilePhoneTextView);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Khởi tạo Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem thông tin", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(currentUser.getUid());

        // Tải thông tin tài khoản
        loadProfile();

        // Xử lý nút chỉnh sửa
        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());

        // Xử lý nút đăng xuất
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
    }

    private void loadProfile() {
        databaseReference.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String name = dataSnapshot.child("name").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String studentId = dataSnapshot.child("studentId").getValue(String.class);
                String phone = dataSnapshot.child("phone").getValue(String.class);

                profileNameTextView.setText("Tên: " + (name != null ? name : "Chưa cập nhật"));
                profileEmailTextView.setText("Email: " + (email != null ? email : currentUser.getEmail()));
                profileStudentIdTextView.setText("Mã sinh viên: " + (studentId != null ? studentId : "Chưa cập nhật"));
                profilePhoneTextView.setText("SĐT: " + (phone != null ? phone : "Chưa cập nhật"));
            }
        }).addOnFailureListener(e ->
                Toast.makeText(ProfileActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
        );
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chỉnh sửa thông tin");

        // Layout chứa các trường nhập
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Trường nhập Tên
        EditText inputName = new EditText(this);
        inputName.setHint("Tên mới");
        inputName.setInputType(InputType.TYPE_CLASS_TEXT);
        layout.addView(inputName);

        // Trường nhập Mã sinh viên
        EditText inputStudentId = new EditText(this);
        inputStudentId.setHint("Mã sinh viên mới");
        inputStudentId.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(inputStudentId);

        // Trường nhập Số điện thoại
        EditText inputPhone = new EditText(this);
        inputPhone.setHint("SĐT mới");
        inputPhone.setInputType(InputType.TYPE_CLASS_PHONE);
        layout.addView(inputPhone);

        builder.setView(layout);

        // Xử lý nút Lưu
        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = inputName.getText().toString().trim();
            String newStudentId = inputStudentId.getText().toString().trim();
            String newPhone = inputPhone.getText().toString().trim();

            if (newName.isEmpty() || newStudentId.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            } else {
                databaseReference.child("name").setValue(newName);
                databaseReference.child("studentId").setValue(newStudentId);
                databaseReference.child("phone").setValue(newPhone);
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                loadProfile();
            }
        });

        // Nút Hủy
        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất không?");

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(ProfileActivity.this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.show();
    }
}
