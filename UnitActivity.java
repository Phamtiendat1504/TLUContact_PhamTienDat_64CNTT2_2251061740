package com.example.tlu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UnitActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UnitAdapter unitAdapter;
    private List<Unit> unitList;
    private DatabaseReference databaseReference;
    private TextInputEditText searchEditText;
    private FloatingActionButton btnSort;
    private boolean isSortedAscending = true; // true: A->Z, false: Z->A

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit);

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Khởi tạo RecyclerView
        recyclerView = findViewById(R.id.unitRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        unitList = new ArrayList<>();
        unitAdapter = new UnitAdapter(unitList, this);
        recyclerView.setAdapter(unitAdapter);

        // Khởi tạo Firebase
        databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("units");

        // Tải dữ liệu từ Firebase và thêm dữ liệu mặc định nếu cần
        loadUnits();

        // Thiết lập tìm kiếm
        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUnits(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thiết lập nút sắp xếp
        btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> {
            sortUnits();
            isSortedAscending = !isSortedAscending;
            // Cập nhật biểu tượng nút để phản ánh trạng thái sắp xếp
            btnSort.setImageResource(isSortedAscending ? R.drawable.ic_sort_asc : R.drawable.ic_sort_desc);
        });
    }

    private void loadUnits() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                unitList.clear();
                if (!dataSnapshot.exists()) {
                    // Nếu không có dữ liệu trên Firebase, thêm dữ liệu mặc định
                    addDefaultUnitsToFirebase();
                } else {
                    // Tải dữ liệu từ Firebase
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Unit unit = snapshot.getValue(Unit.class);
                        if (unit != null) {
                            unit.setId(snapshot.getKey());
                            unitList.add(unit);
                        }
                    }
                    // Sắp xếp mặc định A->Z khi tải dữ liệu
                    sortUnitsAscending();
                    unitAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(UnitActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                unitList.addAll(getDefaultUnitList());
                sortUnitsAscending();
                unitAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addDefaultUnitsToFirebase() {
        List<Unit> defaultUnits = getDefaultUnitList();
        for (Unit unit : defaultUnits) {
            String unitId = databaseReference.push().getKey();
            databaseReference.child(unitId).setValue(unit)
                    .addOnSuccessListener(aVoid -> {
                        unitList.add(unit);
                        sortUnitsAscending(); // Sắp xếp sau khi thêm
                        unitAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(UnitActivity.this, "Không thể thêm dữ liệu mặc định: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private List<Unit> getDefaultUnitList() {
        List<Unit> units = new ArrayList<>();
        units.add(new Unit("Văn phòng Hiệu trưởng", "024 3865 1234", "vpht@tlu.edu.vn"));
        units.add(new Unit("Phòng Tổ chức - Cán bộ", "024 3865 2345", "tccb@tlu.edu.vn"));
        units.add(new Unit("Trung tâm Công nghệ Mới", "024 3865 3456", "ttcn@tlu.edu.vn"));
        units.add(new Unit("Khoa Công trình Thủy lợi", "024 3865 4567", "cttl@tlu.edu.vn"));
        units.add(new Unit("Phòng Quan hệ Quốc tế", "024 3865 5678", "qhqt@tlu.edu.vn"));
        units.add(new Unit("Trạm Thực nghiệm Thủy lợi", "024 3865 6789", "tttntl@tlu.edu.vn"));
        units.add(new Unit("Ban Quản lý Ký túc xá", "024 3865 7890", "qlktx@tlu.edu.vn"));
        units.add(new Unit("Trung tâm Đào tạo Từ xa", "024 3865 8901", "dttx@tlu.edu.vn"));
        units.add(new Unit("Phòng Hợp tác Doanh nghiệp", "024 3865 9012", "htdn@tlu.edu.vn"));
        units.add(new Unit("Khoa Sau Đại học", "024 3865 0123", "sdh@tlu.edu.vn"));
        return units;
    }

    private void filterUnits(String query) {
        List<Unit> filteredList = new ArrayList<>();
        for (Unit unit : unitList) {
            if (unit.getName().toLowerCase().contains(query.toLowerCase()) ||
                    unit.getPhone().toLowerCase().contains(query.toLowerCase()) ||
                    unit.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(unit);
            }
        }
        unitAdapter.updateList(filteredList);
    }

    private void sortUnits() {
        if (isSortedAscending) {
            sortUnitsAscending();
        } else {
            sortUnitsDescending();
        }
        unitAdapter.notifyDataSetChanged();
    }

    private void sortUnitsAscending() {
        Collections.sort(unitList, new Comparator<Unit>() {
            @Override
            public int compare(Unit u1, Unit u2) {
                return u1.getName().compareToIgnoreCase(u2.getName());
            }
        });
    }

    private void sortUnitsDescending() {
        Collections.sort(unitList, new Comparator<Unit>() {
            @Override
            public int compare(Unit u1, Unit u2) {
                return u2.getName().compareToIgnoreCase(u1.getName());
            }
        });
    }

    // Lớp Unit để lưu dữ liệu đơn vị
    public static class Unit {
        private String id;
        private String name;
        private String phone;
        private String email;

        public Unit() {
            // Constructor mặc định cần cho Firebase
        }

        public Unit(String name, String phone, String email) {
            this.name = name;
            this.phone = phone;
            this.email = email;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public String getEmail() {
            return email;
        }
    }
}