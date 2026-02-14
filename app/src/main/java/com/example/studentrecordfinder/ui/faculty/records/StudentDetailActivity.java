package com.example.studentrecordfinder.ui.faculty.records;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.google.firebase.database.FirebaseDatabase;

public class StudentDetailActivity extends AppCompatActivity {

    private String studentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentId = getIntent().getStringExtra("student_id");

        if (studentId == null) {
            Toast.makeText(this, "Invalid student", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView txtName = findViewById(R.id.txtStudentName);
        TextView txtMeta = findViewById(R.id.txtStudentMeta);
        RecyclerView recycler = findViewById(R.id.recyclerSubjects);

        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);

        FirebaseDatabase.getInstance()
                .getReference("students")
                .child(studentId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    txtName.setText(snapshot.child("name").getValue(String.class));
                    txtMeta.setText(
                            "Roll " + snapshot.child("roll_no").getValue(String.class)
                                    + " • " + snapshot.child("department").getValue(String.class)
                    );

                    recycler.setAdapter(
                            new SubjectRecordAdapter(
                                    snapshot.child("attendance"),
                                    snapshot.child("marks"),
                                    studentId
                            )
                    );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load student", Toast.LENGTH_SHORT).show()
                );
    }
}
