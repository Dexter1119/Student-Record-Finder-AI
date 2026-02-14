package com.example.studentrecordfinder.ui.faculty.records;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.studentrecordfinder.controller.FacultyController;
import com.example.studentrecordfinder.model.Student;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FacultyRecordsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StudentAdapter adapter;
    private final List<Student> students = new ArrayList<>();
    private FacultyController facultyController;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.fragment_faculty_records, container, false);

        recyclerView = view.findViewById(R.id.recyclerStudents);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        adapter = new StudentAdapter(students);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fabAddStudent);
        fab.setOnClickListener(v ->
                startActivity(new Intent(
                        getContext(),
                        AddStudentActivity.class
                ))
        );

        facultyController = new FacultyController();
        loadStudents();



        return view;
    }

    // ==================================================
    // AUTO REFRESH AFTER ADD STUDENT
    // ==================================================
    @Override
    public void onResume() {
        super.onResume();
        loadStudents();
    }

    // ==================================================
    // LOAD STUDENTS OWNED BY FACULTY
    // ==================================================
    private void loadStudents() {

        facultyController.fetchAllStudents(new FacultyController.FacultyCallback() {

            @Override
            public void onSuccess(DataSnapshot snapshot) {

                students.clear();

                for (DataSnapshot s : snapshot.getChildren()) {
                    Student student = s.getValue(Student.class);
                    if (student != null) {
                        student.student_id = s.getKey(); // CRITICAL
                        students.add(student);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
