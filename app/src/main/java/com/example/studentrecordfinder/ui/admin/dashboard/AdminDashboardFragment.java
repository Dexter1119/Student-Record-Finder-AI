package com.example.studentrecordfinder.ui.admin.dashboard;

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
import com.example.studentrecordfinder.ui.admin.students.AdminStudentsFragment;
import com.example.studentrecordfinder.ui.admin.faculty.AdminFacultyFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class AdminDashboardFragment extends Fragment {

    private TextView txtTotalStudents;
    private TextView txtTotalFaculty;
    private TextView txtActiveFaculty;
    private TextView txtPendingFaculty;
    private TextView txtAIInsight;
    private TextView txtAtRiskStudents;
    private TextView txtSystemHealth;

    private ProgressBar progressSystemHealth;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View v = inflater.inflate(R.layout.fragment_admin_dashboard, container, false);

        txtTotalStudents  = v.findViewById(R.id.txtTotalStudents);
        txtTotalFaculty   = v.findViewById(R.id.txtTotalFaculty);
        txtActiveFaculty  = v.findViewById(R.id.txtActiveFaculty);
        txtPendingFaculty = v.findViewById(R.id.txtPendingFaculty);
        txtAIInsight      = v.findViewById(R.id.txtAIInsight);
        txtAtRiskStudents = v.findViewById(R.id.txtAtRiskStudents);
        txtSystemHealth   = v.findViewById(R.id.txtSystemHealth);
        progressSystemHealth = v.findViewById(R.id.progressSystemHealth);

        setupNavigation(v);
        loadStats();

        return v;
    }

    // ==================================================
    // NAVIGATION
    // ==================================================
    private void setupNavigation(View v) {

        v.findViewById(R.id.cardStudents).setOnClickListener(view ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.adminFragmentContainer, new AdminStudentsFragment())
                        .addToBackStack(null)
                        .commit()
        );

        v.findViewById(R.id.cardFaculty).setOnClickListener(view ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.adminFragmentContainer, new AdminFacultyFragment())
                        .addToBackStack(null)
                        .commit()
        );
    }

    // ==================================================
    // LOAD ADMIN STATS
    // ==================================================
    private void loadStats() {

        FirebaseDatabase.getInstance()
                .getReference()
                .get()
                .addOnSuccessListener(root -> {

                    DataSnapshot studentsSnap = root.child("students");
                    DataSnapshot facultySnap  = root.child("faculty");

                    long totalStudents = studentsSnap.getChildrenCount();
                    long totalFaculty  = facultySnap.getChildrenCount();

                    long activeFaculty = 0;
                    long pendingFaculty = 0;
                    long atRiskStudents = 0;

                    // ---------- FACULTY ANALYSIS ----------
                    for (DataSnapshot f : facultySnap.getChildren()) {

                        Boolean approved = f.child("approved").getValue(Boolean.class);
                        Boolean active   = f.child("active").getValue(Boolean.class);

                        if (approved != null && approved && (active == null || active)) {
                            activeFaculty++;
                        } else {
                            pendingFaculty++;
                        }
                    }

                    // ---------- STUDENT RISK ANALYSIS ----------
                    for (DataSnapshot s : studentsSnap.getChildren()) {

                        DataSnapshot attendance = s.child("attendance");

                        if (!attendance.exists()) continue;

                        for (DataSnapshot sub : attendance.getChildren()) {
                            Integer pct = sub.child("percentage").getValue(Integer.class);

                            if (pct != null && pct < 75) {
                                atRiskStudents++;
                                break; // count student once
                            }
                        }
                    }

                    // ---------- UPDATE UI ----------
                    txtTotalStudents.setText(String.valueOf(totalStudents));
                    txtTotalFaculty.setText(String.valueOf(totalFaculty));
                    txtActiveFaculty.setText("Active: " + activeFaculty);
                    txtPendingFaculty.setText("Pending: " + pendingFaculty);
                    txtAtRiskStudents.setText(String.valueOf(atRiskStudents));

                    int healthScore = calculateSystemHealth(
                            totalStudents,
                            atRiskStudents,
                            pendingFaculty
                    );

                    txtSystemHealth.setText(healthScore + "%");
                    progressSystemHealth.setProgress(healthScore);

                    generateSmartInsight(
                            totalStudents,
                            atRiskStudents,
                            pendingFaculty,
                            healthScore
                    );
                });
    }

    // ==================================================
    // HEALTH SCORE
    // ==================================================
    private int calculateSystemHealth(
            long totalStudents,
            long atRiskStudents,
            long pendingFaculty
    ) {

        if (totalStudents == 0) return 100;

        double riskImpact = ((double) atRiskStudents / totalStudents) * 100;
        double facultyImpact = pendingFaculty > 0 ? 10 : 0;

        int health = (int) (100 - riskImpact - facultyImpact);

        if (health < 0) health = 0;

        return health;
    }

    // ==================================================
    // SMART INSIGHT ENGINE
    // ==================================================
    private void generateSmartInsight(
            long totalStudents,
            long atRisk,
            long pendingFaculty,
            int healthScore
    ) {

        String insight;

        if (healthScore >= 90) {
            insight = "Institution performing at optimal stability.";
        }
        else if (atRisk > 0) {
            insight = atRisk + " students are at academic risk. Immediate monitoring recommended.";
        }
        else if (pendingFaculty > 0) {
            insight = "Pending faculty approvals detected. Review to stabilize governance.";
        }
        else {
            insight = "System stable but continuous monitoring advised.";
        }

        txtAIInsight.setText(insight);
    }
}
