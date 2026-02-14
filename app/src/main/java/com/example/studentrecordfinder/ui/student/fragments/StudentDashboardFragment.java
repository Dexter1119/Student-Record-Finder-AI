package com.example.studentrecordfinder.ui.student.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.StudentAiAnalyzer;
import com.example.studentrecordfinder.controller.StudentController;
import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.ResultRecord;
import com.example.studentrecordfinder.model.Student;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.Map;

public class StudentDashboardFragment extends Fragment {

    private TextView tvName, tvRoll, tvDept;
    private TextView tvAttendance, tvGpa;
    private TextView tvAiAlerts, tvAiSuggestions;

    private ProgressBar progressAttendance;



    private StudentController controller;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_student_dashboard,
                container,
                false
        );

        bindViews(view);
        controller = new StudentController();
        loadStudentProfile();


        return view;
    }

    // ---------------- VIEW BINDING ----------------
    private void bindViews(View v) {
        tvName = v.findViewById(R.id.tvStudentName);
        tvRoll = v.findViewById(R.id.tvRoll);
        tvDept = v.findViewById(R.id.tvDepartment);
        tvAttendance = v.findViewById(R.id.tvAttendancePercent);
        tvGpa = v.findViewById(R.id.tvGpaValue);
        tvAiAlerts = v.findViewById(R.id.tvAiAlerts);
        tvAiSuggestions = v.findViewById(R.id.tvAiSuggestions);
        progressAttendance = v.findViewById(R.id.progressAttendance);


    }

    // ---------------- LOAD STUDENT DATA ----------------
    private void loadStudentProfile() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        controller.fetchMyProfile(uid, new StudentController.StudentCallback() {

            @Override
            public void onSuccess(Student student) {

                if (student == null) return;

                // ---------------- PROFILE INFO ----------------
                tvName.setText(student.name != null ? student.name : "—");
                tvRoll.setText("Roll No: " +
                        (student.roll_no != null ? student.roll_no : "—"));
                tvDept.setText(student.department != null ? student.department : "—");

                // ---------------- ATTENDANCE ----------------
                int attendance = calculateAttendance(student);
                tvAttendance.setText(attendance + "%");
                progressAttendance.setProgress(attendance);

                // Dynamic color based on risk
                if (attendance < 75) {
                    tvAttendance.setTextColor(
                            getResources().getColor(R.color.error_red)
                    );
                } else {
                    tvAttendance.setTextColor(
                            getResources().getColor(R.color.primary)
                    );
                }

                // ---------------- GPA ----------------
                String gpa = calculateGpa(student);
                tvGpa.setText(gpa);

                // ---------------- AI ANALYSIS ----------------
                List<String> alerts =
                        StudentAiAnalyzer.generateAlerts(student);

                List<String> tips =
                        StudentAiAnalyzer.getSuggestions(student);

                // Format nicely with bullet points
                tvAiAlerts.setText(
                        android.text.TextUtils.join("\n• ", alerts)
                );

                tvAiSuggestions.setText(
                        android.text.TextUtils.join("\n• ", tips)
                );
            }


            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // ---------------- CALCULATIONS ----------------
    private int calculateAttendance(Student student) {

        if (student.attendance == null || student.attendance.isEmpty())
            return 0;

        int sum = 0;
        int count = 0;

        for (Map.Entry<String, AttendanceRecord> entry
                : student.attendance.entrySet()) {

            AttendanceRecord record = entry.getValue();
            sum += record.percentage;
            count++;
        }

        return count == 0 ? 0 : sum / count;
    }

    private String calculateGpa(Student student) {

        if (student.marks == null || student.marks.isEmpty())
            return "0.0";

        int totalMarks = 0;
        int subjects = 0;

        for (Map.Entry<String, ResultRecord> entry
                : student.marks.entrySet()) {

            ResultRecord record = entry.getValue();
            totalMarks += record.total;
            subjects++;
        }

        if (subjects == 0) return "0.0";

        double avg = (double) totalMarks / subjects;
        double gpa = (avg / 100.0) * 4.0;

        return String.format("%.2f", gpa);
    }
}
