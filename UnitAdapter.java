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

public class UnitAdapter extends RecyclerView.Adapter<UnitAdapter.UnitViewHolder> {

    private List<UnitActivity.Unit> unitList;
    private Context context;

    public UnitAdapter(List<UnitActivity.Unit> unitList, Context context) {
        this.unitList = unitList;
        this.context = context;
    }

    @Override
    public UnitViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_unit, parent, false);
        return new UnitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UnitViewHolder holder, int position) {
        UnitActivity.Unit unit = unitList.get(position);
        holder.unitNameTextView.setText(unit.getName());
        holder.unitPhoneTextView.setText("SĐT: " + unit.getPhone());
        holder.unitEmailTextView.setText("Email: " + unit.getEmail());

        // Xử lý nút Edit
        holder.btnEdit.setOnClickListener(v -> showEditDialog(unit, position));

        // Xử lý nút Delete
        holder.btnDelete.setOnClickListener(v -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("units").child(unit.getId());
            databaseReference.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Đã xóa " + unit.getName(), Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(context, "Xóa thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    private void showEditDialog(UnitActivity.Unit unit, int position) {
        // Tạo dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_unit, null);
        builder.setView(dialogView);

        // Ánh xạ các trường trong dialog
        EditText editName = dialogView.findViewById(R.id.editUnitName);
        EditText editPhone = dialogView.findViewById(R.id.editUnitPhone);
        EditText editEmail = dialogView.findViewById(R.id.editUnitEmail);

        // Điền thông tin hiện tại vào dialog
        editName.setText(unit.getName());
        editPhone.setText(unit.getPhone());
        editEmail.setText(unit.getEmail());

        builder.setTitle("Chỉnh sửa đơn vị")
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newName = editName.getText().toString().trim();
                    String newPhone = editPhone.getText().toString().trim();
                    String newEmail = editEmail.getText().toString().trim();

                    if (newName.isEmpty() || newPhone.isEmpty() || newEmail.isEmpty()) {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Cập nhật thông tin đơn vị
                    UnitActivity.Unit updatedUnit = new UnitActivity.Unit(newName, newPhone, newEmail);
                    updatedUnit.setId(unit.getId());

                    DatabaseReference databaseReference = FirebaseDatabase.getInstance("https://tlucontact-3d1de-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("units").child(unit.getId());
                    databaseReference.setValue(updatedUnit)
                            .addOnSuccessListener(aVoid -> {
                                unitList.set(position, updatedUnit);
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
        return unitList.size();
    }

    public void updateList(List<UnitActivity.Unit> newList) {
        unitList.clear();
        unitList.addAll(newList);
        notifyDataSetChanged();
    }

    static class UnitViewHolder extends RecyclerView.ViewHolder {
        TextView unitNameTextView, unitPhoneTextView, unitEmailTextView;
        MaterialButton btnEdit, btnDelete;

        UnitViewHolder(View itemView) {
            super(itemView);
            unitNameTextView = itemView.findViewById(R.id.unitNameTextView);
            unitPhoneTextView = itemView.findViewById(R.id.unitPhoneTextView);
            unitEmailTextView = itemView.findViewById(R.id.unitEmailTextView);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}