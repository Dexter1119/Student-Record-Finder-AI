package com.example.studentrecordfinder.ui.admin.students;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.ResultRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminStudentDetailActivity extends AppCompatActivity {

    private TextView txtName, txtRoll, txtDept;
    private RecyclerView recycler;
    private StudentAcademicAdapter adapter;

    private final List<StudentAcademicRow> rows = new ArrayList<>();
    private String studentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_student_detail);

        studentUid = getIntent().getStringExtra("student_uid");
        if (studentUid == null) {
            finish();
            return;
        }

        txtName = findViewById(R.id.txtStudentName);
        txtRoll = findViewById(R.id.txtStudentRoll);
        txtDept = findViewById(R.id.txtStudentDept);

        recycler = findViewById(R.id.recyclerAcademic);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new StudentAcademicAdapter(rows);
        recycler.setAdapter(adapter);

        loadStudent();
    }

    // ==================================================
    // LOAD STUDENT + ACADEMICS
    // ==================================================
    private void loadStudent() {

        FirebaseDatabase.getInstance()
                .getReference("students")
                .child(studentUid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        Toast.makeText(this, "Student not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    txtName.setText(snapshot.child("name").getValue(String.class));
                    txtRoll.setText("Roll: " + snapshot.child("roll_no").getValue(String.class));
                    txtDept.setText("Dept: " + snapshot.child("department").getValue(String.class));

                    rows.clear();

                    DataSnapshot attendanceSnap = snapshot.child("attendance");
                    DataSnapshot marksSnap = snapshot.child("marks");

                    for (DataSnapshot subjectSnap : attendanceSnap.getChildren()) {

                        String subject = subjectSnap.getKey();

                        AttendanceRecord ar =
                                subjectSnap.getValue(AttendanceRecord.class);

                        ResultRecord rr =
                                marksSnap.child(subject).getValue(ResultRecord.class);

                        if (ar != null && rr != null) {
                            rows.add(new StudentAcademicRow(
                                    subject,
                                    ar.percentage,
                                    rr.internal,
                                    rr.external,
                                    rr.total
                            ));
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load student data", Toast.LENGTH_SHORT).show()
                );
    }

}
