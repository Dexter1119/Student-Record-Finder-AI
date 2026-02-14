package com.example.studentrecordfinder.ui.admin.students;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.Faculty;
import com.example.studentrecordfinder.model.Student;
import com.google.firebase.database.*;

import java.util.*;

public class AdminStudentsFragment extends Fragment {

    private static final String TAG = "AdminStudentsFragment";

    private RecyclerView recyclerView;
    private AdminStudentAdapter adapter;
    private final List<Student> studentList = new ArrayList<>();
    private final Map<String, String> facultyNameCache = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_admin_students, container, false);

        recyclerView = view.findViewById(R.id.recyclerStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AdminStudentAdapter(studentList, facultyNameCache);
        recyclerView.setAdapter(adapter);

        fetchAllStudents();
        return view;
    }

    // ==================================================
    // FETCH ALL STUDENTS (ADMIN)
    // ==================================================
    private void fetchAllStudents() {

        FirebaseDatabase.getInstance()
                .getReference("students")
                .get()
                .addOnSuccessListener(snapshot -> {

                    studentList.clear();

                    for (DataSnapshot s : snapshot.getChildren()) {
                        Student st = s.getValue(Student.class);
                        if (st != null) {
                            st.student_id = s.getKey();
                            studentList.add(st);
                            fetchFacultyName(st.faculty_id);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(),
                            "Failed to load students",
                            Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error", e);
                });
    }

    // ==================================================
    // FETCH FACULTY NAME (CACHE)
    // ==================================================
    private void fetchFacultyName(String facultyId) {

        if (facultyNameCache.containsKey(facultyId)) return;

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(facultyId)
                .child("name")
                .get()
                .addOnSuccessListener(s -> {
                    if (s.exists()) {
                        facultyNameCache.put(facultyId, s.getValue(String.class));
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
