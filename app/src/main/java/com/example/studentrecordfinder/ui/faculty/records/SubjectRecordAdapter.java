package com.example.studentrecordfinder.ui.faculty.records;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.AttendanceController;
import com.example.studentrecordfinder.controller.ResultController;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SubjectRecordAdapter extends RecyclerView.Adapter<SubjectRecordAdapter.VH> {

    private final List<DataSnapshot> subjects = new ArrayList<>();
    private final DataSnapshot marks;
    private final String studentId;

    public SubjectRecordAdapter(
            DataSnapshot attendance,
            DataSnapshot marks,
            String studentId
    ) {
        for (DataSnapshot s : attendance.getChildren()) {
            subjects.add(s);
        }
        this.marks = marks;
        this.studentId = studentId;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subject_record, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        DataSnapshot subjectSnap = subjects.get(pos);
        String subject = subjectSnap.getKey();

        Integer percentage =
                subjectSnap.child("percentage").getValue(Integer.class);

        Integer totalMarks = 0;
        if (marks != null && subject != null && marks.child(subject).exists()) {
            Integer tmp = marks.child(subject)
                    .child("total_marks")
                    .getValue(Integer.class);
            totalMarks = tmp != null ? tmp : 0;
        }

        h.txtSubject.setText(subject != null ? subject : "—");
        h.txtAttendance.setText("Attendance: " + (percentage != null ? percentage : 0) + "%");
        h.txtMarks.setText("Marks: " + totalMarks);

        h.btnUpdate.setOnClickListener(v ->
                showUpdateDialog(v, subject));
    }

    @Override
    public int getItemCount() {
        return subjects.size();
    }

    // ----------------------------------
    // UPDATE DIALOG
    // ----------------------------------
    private void showUpdateDialog(View v, String subject) {

        View dialogView = LayoutInflater.from(v.getContext())
                .inflate(R.layout.dialog_update_record, null);

        EditText edtAttended = dialogView.findViewById(R.id.edtAttended);
        EditText edtTotal = dialogView.findViewById(R.id.edtTotal);
        EditText edtInternal = dialogView.findViewById(R.id.edtInternal);
        EditText edtExternal = dialogView.findViewById(R.id.edtExternal);

        new AlertDialog.Builder(v.getContext())
                .setTitle("Update " + subject)
                .setView(dialogView)
                .setPositiveButton("Update", (d, i) -> {

                    try {
                        String att = edtAttended.getText().toString().trim();
                        String tot = edtTotal.getText().toString().trim();
                        String intern = edtInternal.getText().toString().trim();
                        String extern = edtExternal.getText().toString().trim();

                        if (!att.isEmpty() && !tot.isEmpty()) {
                            int attended = Integer.parseInt(att);
                            int total = Integer.parseInt(tot);

                            if (total > 0) {
                                AttendanceController.update(
                                        studentId, subject, attended, total
                                );
                            }
                        }

                        if (!intern.isEmpty() && !extern.isEmpty()) {
                            ResultController.update(
                                    studentId,
                                    subject,
                                    Integer.parseInt(intern),
                                    Integer.parseInt(extern)
                            );
                        }

                        Toast.makeText(v.getContext(),
                                "Records updated",
                                Toast.LENGTH_SHORT).show();

                    } catch (NumberFormatException e) {
                        Toast.makeText(v.getContext(),
                                "Enter valid numbers",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ----------------------------------
    // VIEW HOLDER
    // ----------------------------------
    static class VH extends RecyclerView.ViewHolder {

        TextView txtSubject, txtAttendance, txtMarks;
        Button btnUpdate;

        VH(View v) {
            super(v);
            txtSubject = v.findViewById(R.id.txtSubjectName);
            txtAttendance = v.findViewById(R.id.txtAttendance);
            txtMarks = v.findViewById(R.id.txtMarks);
            btnUpdate = v.findViewById(R.id.btnUpdate);
        }
    }
}
