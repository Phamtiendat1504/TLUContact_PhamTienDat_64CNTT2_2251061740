package com.example.tlu;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.StaffViewHolder> {

    private List<StaffActivity.Staff> staffList;
    private Context context;

    public StaffAdapter(List<StaffActivity.Staff> staffList, Context context) {
        this.staffList = staffList;
        this.context = context;
    }

    @Override
    public StaffViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_staff, parent, false);
        return new StaffViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StaffViewHolder holder, int position) {
        StaffActivity.Staff staff = staffList.get(position);
        holder.staffNameTextView.setText(staff.getName());
        holder.staffUnitTextView.setText("Đơn vị: " + staff.getUnit());
        holder.staffPhoneTextView.setText("SĐT: " + staff.getPhone());
        holder.staffEmailTextView.setText("Email: " + staff.getEmail());

        holder.btnEdit.setOnClickListener(v -> showEditDialog(staff, position));

        holder.btnDelete.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("staff").child(staff.getId());
            databaseReference.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa " + staff.getName(), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void showEditDialog(StaffActivity.Staff staff, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_staff, null);
        builder.setView(dialogView);

        EditText editName = dialogView.findViewById(R.id.editStaffName);
        EditText editUnit = dialogView.findViewById(R.id.editStaffUnit);
        EditText editPhone = dialogView.findViewById(R.id.editStaffPhone);
        EditText editEmail = dialogView.findViewById(R.id.editStaffEmail);

        editName.setText(staff.getName());
        editUnit.setText(staff.getUnit());
        editPhone.setText(staff.getPhone());
        editEmail.setText(staff.getEmail());

        builder.setTitle("Chỉnh sửa cán bộ")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = editName.getText().toString().trim();
                    String newUnit = editUnit.getText().toString().trim();
                    String newPhone = editPhone.getText().toString().trim();
                    String newEmail = editEmail.getText().toString().trim();

                    if (newName.isEmpty() || newUnit.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    StaffActivity.Staff updatedStaff = new StaffActivity.Staff(newName, newUnit, newPhone, newEmail);
                    updatedStaff.setId(staff.getId());

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("staff").child(staff.getId());
                    databaseReference.setValue(updatedStaff)
                            .addOnSuccessListener(aVoid -> {
                                staffList.set(position, updatedStaff);
                                notifyItemChanged(position);
                                Toast.makeText(context, "Đã cập nhật " + newName, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(context, "Cập nhật thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    @Override
    public int getItemCount() {
        return staffList.size();
    }

    public void updateList(List<StaffActivity.Staff> newList) {
        staffList.clear();
        staffList.addAll(newList);
        notifyDataSetChanged();
    }

    static class StaffViewHolder extends RecyclerView.ViewHolder {
        TextView staffNameTextView, staffUnitTextView, staffPhoneTextView, staffEmailTextView;
        MaterialButton btnEdit, btnDelete;

        StaffViewHolder(View itemView) {
            super(itemView);
            staffNameTextView = itemView.findViewById(R.id.staffNameTextView);
            staffUnitTextView = itemView.findViewById(R.id.staffUnitTextView);
            staffPhoneTextView = itemView.findViewById(R.id.staffPhoneTextView);
            staffEmailTextView = itemView.findViewById(R.id.staffEmailTextView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}