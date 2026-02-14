package com.example.studentrecordfinder.ui.faculty.records;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.Student;

import java.util.List;
import java.util.Map;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final List<Student> students;

    public StudentAdapter(List<Student> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_record, parent, false);
        return new StudentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {

        Student s = students.get(position);

        holder.txtName.setText(
                s.name != null ? s.name : "Unnamed Student"
        );

        holder.txtMeta.setText(
                "Roll " + (s.roll_no != null ? s.roll_no : "—")
                        + " • "
                        + (s.department != null ? s.department : "—")
        );

        int percentage = extractAttendancePercentage(s.attendance);
        holder.txtAttendance.setText("Attendance: " + percentage + "%");

        // Risk indicator
        if (percentage < 75) {
            holder.riskDot.setBackgroundResource(R.drawable.bg_risk_dot);
        } else {
            holder.riskDot.setBackgroundResource(R.drawable.bg_safe_dot);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), StudentDetailActivity.class);
            intent.putExtra("student_id", s.student_id);
            v.getContext().startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v ->
                deleteStudent(v, s, holder.getAdapterPosition())
        );


    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    // -------------------------------
    // HELPERS
    // -------------------------------
    private int extractAttendancePercentage(Map<String, AttendanceRecord> attendance) {

        if (attendance == null || attendance.isEmpty()) return 0;

        int sum = 0;
        int count = 0;

        for (AttendanceRecord a : attendance.values()) {
            sum += a.percentage;
            count++;
        }
        return count == 0 ? 0 : sum / count;
    }

    private void deleteStudent(View view, Student student, int position) {

        new androidx.appcompat.app.AlertDialog.Builder(view.getContext())
                .setTitle("Remove Student")
                .setMessage("Are you sure you want to remove this student?")
                .setPositiveButton("Yes", (dialog, which) -> {

                    String uid = student.student_id;

                    com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("students")
                            .child(uid)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> {

                                // Also remove from users node
                                com.google.firebase.database.FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(uid)
                                        .removeValue();

                                students.remove(position);
                                notifyItemRemoved(position);

                                android.widget.Toast.makeText(
                                        view.getContext(),
                                        "Student removed",
                                        android.widget.Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e ->
                                    android.widget.Toast.makeText(
                                            view.getContext(),
                                            "Failed to delete",
                                            android.widget.Toast.LENGTH_SHORT
                                    ).show()
                            );

                })
                .setNegativeButton("Cancel", null)
                .show();
    }





    // -------------------------------
    // VIEW HOLDER
    // -------------------------------
    static class StudentViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtMeta, txtAttendance;
        View riskDot;
        ImageView btnDelete;

        StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtStudentName);
            txtMeta = itemView.findViewById(R.id.txtStudentMeta);
            txtAttendance = itemView.findViewById(R.id.txtAttendance);
            riskDot = itemView.findViewById(R.id.viewRiskDot);
            btnDelete = itemView.findViewById(R.id.btnDelete);

        }
    }
}
