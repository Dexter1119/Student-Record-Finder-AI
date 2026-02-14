package com.example.studentrecordfinder.ui.admin.faculty;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.Faculty;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FacultyAdapter extends RecyclerView.Adapter<FacultyAdapter.VH> {

    private final List<Faculty> list;

    public FacultyAdapter(List<Faculty> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_faculty, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {

        Faculty f = list.get(position);

        h.txtName.setText(f.name);
        h.txtDept.setText(f.department);

        boolean approved = f.approved;
        boolean active = f.active;

        // ---------- STATUS ----------
        if (!approved) {
            h.txtStatus.setText("PENDING APPROVAL");
        } else if (!active) {
            h.txtStatus.setText("DISABLED");
        } else {
            h.txtStatus.setText("ACTIVE");
        }

        // ---------- BUTTON VISIBILITY ----------
        if (!approved) {
            h.btnApprove.setVisibility(View.VISIBLE);
            h.btnReject.setVisibility(View.VISIBLE);
            h.btnDelete.setVisibility(View.GONE);
        } else {
            h.btnApprove.setVisibility(View.GONE);
            h.btnReject.setVisibility(View.GONE);
            h.btnDelete.setVisibility(View.VISIBLE);
        }

        // ---------- ACTIONS ----------
        h.btnApprove.setOnClickListener(v -> approveFaculty(f));
        h.btnReject.setOnClickListener(v -> rejectFaculty(v, f));
        h.btnDelete.setOnClickListener(v -> deleteFaculty(v, f));

        // Edit only if approved
        h.itemView.setOnClickListener(v -> {
            if (approved) {
                Intent i = new Intent(v.getContext(), EditFacultyActivity.class);
                i.putExtra("faculty_uid", f.faculty_id);
                v.getContext().startActivity(i);
            }
        });
    }

    // ==================================================
    // APPROVE FACULTY
    // ==================================================
    private void approveFaculty(Faculty f) {

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(f.faculty_id)
                .child("approved")
                .setValue(true);

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(f.faculty_id)
                .child("approved")
                .setValue(true);

        f.approved = true;
        f.active = true;
        notifyDataSetChanged();
    }

    // ==================================================
    // REJECT FACULTY (ONLY PENDING)
    // ==================================================
    private void rejectFaculty(View v, Faculty f) {

        new AlertDialog.Builder(v.getContext())
                .setTitle("Reject Faculty")
                .setMessage("This will permanently remove the pending faculty account.")
                .setPositiveButton("Reject", (d, w) -> {

                    FirebaseDatabase.getInstance()
                            .getReference("faculty")
                            .child(f.faculty_id)
                            .removeValue();

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(f.faculty_id)
                            .removeValue();

                    list.remove(f);
                    notifyDataSetChanged();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteFaculty(View v, Faculty f) {

        new AlertDialog.Builder(v.getContext())
                .setTitle("Delete Faculty")
                .setMessage("This will permanently remove the faculty and ALL their students.")
                .setPositiveButton("Delete", (d, w) -> {

                    com.google.firebase.database.DatabaseReference root = FirebaseDatabase.getInstance().getReference();

                    // 1️⃣ Delete all students under faculty
                    root.child("students")
                            .orderByChild("faculty_id")
                            .equalTo(f.faculty_id)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                for (com.google.firebase.database.DataSnapshot s : snapshot.getChildren()) {

                                    String studentUid = s.getKey();

                                    root.child("students")
                                            .child(studentUid)
                                            .removeValue();

                                    root.child("users")
                                            .child(studentUid)
                                            .removeValue();
                                }

                                // 2️⃣ Delete faculty
                                root.child("faculty")
                                        .child(f.faculty_id)
                                        .removeValue();

                                root.child("users")
                                        .child(f.faculty_id)
                                        .removeValue();

                                list.remove(f);
                                notifyDataSetChanged();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    // ==================================================
    // VIEW HOLDER
    // ==================================================
    static class VH extends RecyclerView.ViewHolder {

        TextView txtName, txtDept, txtStatus;
        Button btnApprove, btnReject;
        ImageView btnDelete;


        VH(View v) {
            super(v);

            txtName = v.findViewById(R.id.txtFacultyName);
            txtDept = v.findViewById(R.id.txtFacultyDept);
            txtStatus = v.findViewById(R.id.txtFacultyStatus);

            btnApprove = v.findViewById(R.id.btnApprove);
            btnReject = v.findViewById(R.id.btnReject);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }

}
