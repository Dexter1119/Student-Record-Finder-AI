package com.example.studentrecordfinder.ui.admin.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.Faculty;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminFacultyFragment extends Fragment {

    private static final String TAG = "AdminFacultyFragment";

    private RecyclerView recyclerFaculty;
    private FacultyAdapter adapter;
    private final List<Faculty> facultyList = new ArrayList<>();

    private FloatingActionButton fabAddFaculty;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_admin_faculty, container, false);

        recyclerFaculty = view.findViewById(R.id.recyclerFaculty);
        fabAddFaculty = view.findViewById(R.id.fabAddFaculty);

        recyclerFaculty.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerFaculty.setHasFixedSize(true);

        adapter = new FacultyAdapter(facultyList);
        recyclerFaculty.setAdapter(adapter);

        // ➕ ADD FACULTY
        fabAddFaculty.setOnClickListener(v ->
                startActivity(new Intent(getContext(), AddFacultyActivity.class))
        );

        fetchAllFaculty();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAllFaculty(); // 🔄 refresh after add / edit / approve / disable
    }

    // ==================================================
    // FETCH ALL FACULTY (ADMIN MANAGEMENT VIEW)
    // ==================================================
    private void fetchAllFaculty() {

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .get()
                .addOnSuccessListener(snapshot -> {

                    facultyList.clear();

                    for (DataSnapshot s : snapshot.getChildren()) {
                        try {
                            Faculty faculty = s.getValue(Faculty.class);
                            if (faculty != null) {
                                faculty.faculty_id = s.getKey();

                                // Backward compatibility
                                if (!s.hasChild("active")) {
                                    faculty.active = true;
                                }

                                facultyList.add(faculty);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Faculty mapping error: " + s.getKey(), e);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            getContext(),
                            "Failed to load faculty list",
                            Toast.LENGTH_SHORT
                    ).show();
                    Log.e(TAG, "Firebase error", e);
                });
    }
}
