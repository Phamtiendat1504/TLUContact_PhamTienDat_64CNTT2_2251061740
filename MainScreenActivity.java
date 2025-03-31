package com.example.tlu;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        findViewById(R.id.btnDanhBaDonVi).setOnClickListener(v -> {
            Intent intent = new Intent(MainScreenActivity.this, UnitActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnDanhBaCBNV).setOnClickListener(v -> {
            Intent intent = new Intent(MainScreenActivity.this, StaffActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnProfile).setOnClickListener(v -> {
            Intent intent = new Intent(MainScreenActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
}