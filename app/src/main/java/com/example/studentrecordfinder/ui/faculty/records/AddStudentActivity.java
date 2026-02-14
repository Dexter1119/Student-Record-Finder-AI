package com.example.studentrecordfinder.ui.faculty.records;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.ResultRecord;
import com.example.studentrecordfinder.model.Student;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddStudentActivity extends AppCompatActivity {

    private EditText edtName, edtRoll, edtDept, edtEmail, edtPassword;
    private EditText s1, s2, s3, s4, s5;
    private Button btnSave;

    private FirebaseAuth facultyAuth;
    private FirebaseAuth studentAuth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_student);

        facultyAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        initSecondaryAuth();

        edtName = findViewById(R.id.edtName);
        edtRoll = findViewById(R.id.edtRoll);
        edtDept = findViewById(R.id.edtDepartment);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        s1 = findViewById(R.id.edtSubject1);
        s2 = findViewById(R.id.edtSubject2);
        s3 = findViewById(R.id.edtSubject3);
        s4 = findViewById(R.id.edtSubject4);
        s5 = findViewById(R.id.edtSubject5);

        btnSave = findViewById(R.id.btnSaveStudent);
        btnSave.setOnClickListener(v -> createStudent());
    }

    // ==================================================
    // SECONDARY AUTH
    // ==================================================
    private void initSecondaryAuth() {
        FirebaseOptions options = FirebaseOptions.fromResource(this);
        FirebaseApp app;
        try {
            app = FirebaseApp.initializeApp(this, options, "StudentCreation");
        } catch (IllegalStateException e) {
            app = FirebaseApp.getInstance("StudentCreation");
        }
        studentAuth = FirebaseAuth.getInstance(app);
    }

    // ==================================================
    // CREATE STUDENT (AUTH + USERS + SUBJECTS)
    // ==================================================
    private void createStudent() {

        String name = edtName.getText().toString().trim();
        String roll = edtRoll.getText().toString().trim();
        String dept = edtDept.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        String[] subjects = {
                s1.getText().toString().trim(),
                s2.getText().toString().trim(),
                s3.getText().toString().trim(),
                s4.getText().toString().trim(),
                s5.getText().toString().trim()
        };

        if (name.isEmpty() || roll.isEmpty() || dept.isEmpty()
                || email.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String sub : subjects) {
            if (sub.isEmpty()) {
                Toast.makeText(this, "All 5 subjects are required", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        btnSave.setEnabled(false);
        String facultyUid = facultyAuth.getUid();

        studentAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String studentUid = result.getUser().getUid();

                    // Student core data
                    Student student = new Student();
                    student.student_id = studentUid;
                    student.name = name;
                    student.roll_no = roll;
                    student.department = dept;
                    student.email = email;
                    student.approved = true;
                    student.faculty_id = facultyUid;

                        Map<String, AttendanceRecord> attendance = new HashMap<>();
                        Map<String, ResultRecord> marks = new HashMap<>();

                        for (String sub : subjects) {

                            AttendanceRecord ar = new AttendanceRecord();
                            ar.attended = 0;
                            ar.total = 0;
                            ar.percentage = 0;

                            ResultRecord rr = new ResultRecord();
                            rr.internal = 0;
                            rr.external = 0;
                            rr.total = 0;

                            attendance.put(sub, ar);
                            marks.put(sub, rr);
                        }



                    student.attendance = attendance;
                    student.marks = marks;

                    // WRITE STUDENT
                    db.getReference("students").child(studentUid).setValue(student)
                            .addOnSuccessListener(v ->

                                    // WRITE USERS
                                    db.getReference("users").child(studentUid)
                                            .setValue(Map.of(
                                                    "role", "STUDENT",
                                                    "email", email
                                            ))
                                            .addOnSuccessListener(u -> {
                                                Toast.makeText(this,
                                                        "Student created successfully",
                                                        Toast.LENGTH_SHORT).show();
                                                finish();
                                            })
                            );
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
