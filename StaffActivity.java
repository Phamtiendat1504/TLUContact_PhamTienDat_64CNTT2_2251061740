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

public class StaffActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffAdapter staffAdapter;
    private List<Staff> staffList;
    private DatabaseReference databaseReference;
    private TextInputEditText searchEditText;
    private FloatingActionButton btnSort;
    private boolean isSortedAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.staffRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        staffList = new ArrayList<>();
        staffAdapter = new StaffAdapter(staffList, this);
        recyclerView.setAdapter(staffAdapter);

        databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("staff");

        loadStaff();

        searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStaff(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnSort = findViewById(R.id.btnSort);
        btnSort.setOnClickListener(v -> {
            sortStaff();
            isSortedAscending = !isSortedAscending;
            btnSort.setImageResource(isSortedAscending ? R.drawable.ic_sort_asc : R.drawable.ic_sort_desc);
        });
    }

    private void loadStaff() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                staffList.clear();
                if (!dataSnapshot.exists()) {
                    addDefaultStaffToFirebase();
                } else {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Staff staff = snapshot.getValue(Staff.class);
                        if (staff != null) {
                            staff.setId(snapshot.getKey());
                            staffList.add(staff);
                        }
                    }
                    sortStaffAscending();
                    staffAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StaffActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                staffList.addAll(getDefaultStaffList());
                sortStaffAscending();
                staffAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addDefaultStaffToFirebase() {
        List<Staff> defaultStaff = getDefaultStaffList();
        for (Staff staff : defaultStaff) {
            String staffId = databaseReference.push().getKey();
            databaseReference.child(staffId).setValue(staff)
                    .addOnSuccessListener(aVoid -> {
                        staffList.add(staff);
                        sortStaffAscending();
                        staffAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Toast.makeText(StaffActivity.this, "Không thể thêm dữ liệu mặc định: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private List<Staff> getDefaultStaffList() {
        List<Staff> staff = new ArrayList<>();
        staff.add(new Staff("Nguyễn Văn An", "Phòng Đào tạo", "024 3865 1001", "nvan@tlu.edu.vn"));
        staff.add(new Staff("Trần Thị Bình", "Phòng Hành chính", "024 3865 1002", "ttbinh@tlu.edu.vn"));
        staff.add(new Staff("Lê Văn Cường", "Khoa Công trình Thủy lợi", "024 3865 1003", "lvcuong@tlu.edu.vn"));
        staff.add(new Staff("Phạm Thị Dung", "Phòng Quan hệ Quốc tế", "024 3865 1004", "ptdung@tlu.edu.vn"));
        staff.add(new Staff("Hoàng Văn Em", "Trung tâm Công nghệ Mới", "024 3865 1005", "hvem@tlu.edu.vn"));
        staff.add(new Staff("Vũ Thị Hoa", "Ban Quản lý Ký túc xá", "024 3865 1006", "vthoa@tlu.edu.vn"));
        staff.add(new Staff("Đỗ Văn Khánh", "Trạm Thực nghiệm Thủy lợi", "024 3865 1007", "dvkhanh@tlu.edu.vn"));
        staff.add(new Staff("Ngô Thị Lan", "Trung tâm Đào tạo Từ xa", "024 3865 1008", "ntlan@tlu.edu.vn"));
        staff.add(new Staff("Bùi Văn Minh", "Phòng Hợp tác Doanh nghiệp", "024 3865 1009", "bvminh@tlu.edu.vn"));
        staff.add(new Staff("Trịnh Thị Ngọc", "Khoa Sau Đại học", "024 3865 1010", "ttngoc@tlu.edu.vn"));
        return staff;
    }

    private void filterStaff(String query) {
        List<Staff> filteredList = new ArrayList<>();
        for (Staff staff : staffList) {
            if (staff.getName().toLowerCase().contains(query.toLowerCase()) ||
                    staff.getUnit().toLowerCase().contains(query.toLowerCase()) ||
                    staff.getPhone().toLowerCase().contains(query.toLowerCase()) ||
                    staff.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(staff);
            }
        }
        staffAdapter.updateList(filteredList);
    }

    private void sortStaff() {
        if (isSortedAscending) {
            sortStaffAscending();
        } else {
            sortStaffDescending();
        }
        staffAdapter.notifyDataSetChanged();
    }

    private void sortStaffAscending() {
        Collections.sort(staffList, new Comparator<Staff>() {
            @Override
            public int compare(Staff s1, Staff s2) {
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
    }

    private void sortStaffDescending() {
        Collections.sort(staffList, new Comparator<Staff>() {
            @Override
            public int compare(Staff s1, Staff s2) {
                return s2.getName().compareToIgnoreCase(s1.getName());
            }
        });
    }

    public static class Staff {
        private String id;
        private String name;
        private String unit;
        private String phone;
        private String email;

        public Staff() {}

        public Staff(String name, String unit, String phone, String email) {
            this.name = name;
            this.unit = unit;
            this.phone = phone;
            this.email = email;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public String getUnit() { return unit; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
    }
}