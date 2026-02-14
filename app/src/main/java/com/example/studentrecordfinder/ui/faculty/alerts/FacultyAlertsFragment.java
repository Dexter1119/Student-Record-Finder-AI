package com.example.studentrecordfinder.ui.faculty.alerts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.AIChatController;
import com.example.studentrecordfinder.controller.FacultyController;
import com.example.studentrecordfinder.model.AIAlert;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FacultyAlertsFragment extends Fragment {

    private RecyclerView recycler;
    private AlertAdapter adapter;
    private final List<AIAlert> alerts = new ArrayList<>();

    private FacultyController facultyController;
    private AIChatController aiController;
    private boolean loadedOnce = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View v = inflater.inflate(R.layout.fragment_faculty_alerts, container, false);

        recycler = v.findViewById(R.id.recyclerAlerts);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AlertAdapter(alerts);
        recycler.setAdapter(adapter);

        facultyController = new FacultyController();
        aiController = new AIChatController();

        loadAlerts();
        return v;
    }

    // ==================================================
    // LOAD ALERTS
    // ==================================================
    private void loadAlerts() {

        if (loadedOnce) return;
        loadedOnce = true;

        facultyController.fetchAllStudents(new FacultyController.FacultyCallback() {

            @Override
            public void onSuccess(DataSnapshot snapshot) {

                if (!isAdded()) return;

                StringBuilder context = new StringBuilder();
                boolean hasRisk = false;

                for (DataSnapshot s : snapshot.getChildren()) {
                    String name = s.child("name").getValue(String.class);

                    for (DataSnapshot sub : s.child("attendance").getChildren()) {
                        Integer p = sub.child("percentage").getValue(Integer.class);
                        if (p != null && p < 75) {
                            hasRisk = true;
                            context.append(name)
                                    .append(" has ")
                                    .append(p)
                                    .append("% in ")
                                    .append(sub.getKey())
                                    .append(". ");
                        }
                    }
                }

                if (!hasRisk) {
                    showNoAlerts();
                    return;
                }

                aiController.processQuery(
                        "Generate concise faculty alerts from this data:\n" + context,
                        FacultyAlertsFragment.this::parseAlerts
                );
            }

            @Override
            public void onFailure(String error) {
                showErrorAlert(error);
            }
        });
    }


    // ==================================================
    // PARSE AI RESPONSE
    // ==================================================
    private void parseAlerts(String reply) {

        if (!isAdded() || getActivity() == null) return;

        getActivity().runOnUiThread(() -> {

            alerts.clear();

            if (reply == null || reply.trim().isEmpty()) {
                alerts.add(new AIAlert(
                        "No Alerts",
                        "No critical attendance issues detected.",
                        "LOW"
                ));
            } else {
                alerts.add(new AIAlert(
                        "Attendance Risk Alert",
                        reply.trim(),
                        "HIGH"
                ));
            }

            adapter.notifyDataSetChanged();
        });
    }

    // ==================================================
    // HELPERS
    // ==================================================
    private void showNoAlerts() {
        if (!isAdded() || getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            alerts.clear();
            alerts.add(new AIAlert(
                    "All Clear",
                    "No students are currently at attendance risk.",
                    "LOW"
            ));
            adapter.notifyDataSetChanged();
        });
    }

    private void showErrorAlert(String error) {
        if (!isAdded() || getActivity() == null) return;

        getActivity().runOnUiThread(() -> {
            alerts.clear();
            alerts.add(new AIAlert(
                    "Alert Load Failed",
                    error != null ? error : "Unable to load alerts",
                    "MEDIUM"
            ));
            adapter.notifyDataSetChanged();
        });
    }
}
