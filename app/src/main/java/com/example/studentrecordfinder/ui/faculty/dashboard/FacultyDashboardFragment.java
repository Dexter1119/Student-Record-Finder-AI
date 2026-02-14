package com.example.studentrecordfinder.ui.faculty.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.FacultyController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

public class FacultyDashboardFragment extends Fragment {

    private TextView txtName, txtDept;
    private TextView txtStudents, txtRisk, txtSubjects;
    private TextView txtRiskInsight;
    private ProgressBar progressRisk;

    private FacultyController controller;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_faculty_dashboard, container, false);

        txtName = view.findViewById(R.id.txtFacultyName);
        txtDept = view.findViewById(R.id.txtFacultyDept);
        txtStudents = view.findViewById(R.id.txtTotalStudents);
        txtRisk = view.findViewById(R.id.txtAtRisk);
        txtSubjects = view.findViewById(R.id.txtSubjects);
        txtRiskInsight = view.findViewById(R.id.txtRiskInsight);
        progressRisk = view.findViewById(R.id.progressRisk);

        controller = new FacultyController();

        loadFacultyProfile();
        loadStudentStats();

        return view;
    }

    // ==================================================
    // LOAD PROFILE
    // ==================================================
    private void loadFacultyProfile() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            txtName.setText("Session expired");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        controller.fetchFacultyProfile(uid, new FacultyController.FacultyCallback() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                String name = snapshot.child("name").getValue(String.class);
                String dept = snapshot.child("department").getValue(String.class);

                txtName.setText(name != null ? name : "Faculty");
                txtDept.setText(dept != null ? dept : "Department");

                if (snapshot.child("subjects").exists()) {
                    StringBuilder sb = new StringBuilder();
                    for (DataSnapshot s : snapshot.child("subjects").getChildren()) {
                        sb.append("• ")
                                .append(s.getValue(String.class))
                                .append("\n");
                    }
                    txtSubjects.setText(sb.toString());
                } else {
                    txtSubjects.setText("—");
                }
            }

            @Override
            public void onFailure(String error) {
                txtName.setText("Load failed");
                txtDept.setText(error);
            }
        });
    }

    // ==================================================
    // LOAD STUDENT ANALYTICS
    // ==================================================
    private void loadStudentStats() {

        controller.fetchAllStudents(new FacultyController.FacultyCallback() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                int total = 0;
                int risk = 0;

                for (DataSnapshot student : snapshot.getChildren()) {

                    total++;

                    for (DataSnapshot sub : student.child("attendance").getChildren()) {
                        Integer p = sub.child("percentage").getValue(Integer.class);
                        if (p != null && p < 75) {
                            risk++;
                            break;
                        }
                    }
                }

                txtStudents.setText(String.valueOf(total));
                txtRisk.setText(String.valueOf(risk));

                int riskPercent = total == 0 ? 0 : (risk * 100 / total);
                progressRisk.setProgress(riskPercent);

                generateRiskInsight(total, risk, riskPercent);
            }

            @Override
            public void onFailure(String error) {
                txtStudents.setText("—");
                txtRisk.setText("—");
            }
        });
    }

    // ==================================================
    // SMART INSIGHT
    // ==================================================
    private void generateRiskInsight(int total, int risk, int percent) {

        String insight;

        if (percent == 0) {
            insight = "All students are performing within safe academic range.";
        } else if (percent < 20) {
            insight = "Minor academic risk detected. Monitor attendance trends.";
        } else if (percent < 40) {
            insight = "Moderate risk. Consider early intervention strategies.";
        } else {
            insight = "High academic risk detected. Immediate attention required.";
        }

        txtRiskInsight.setText(insight);
    }
}
