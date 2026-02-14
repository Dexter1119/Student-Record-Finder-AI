package com.example.studentrecordfinder.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentrecordfinder.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etEmail, etPassword, etName, etDept, etRoll;
    private Button btnFaculty, btnStudent, btnSignup;

    private String selectedRole = "STUDENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etName = findViewById(R.id.etName);
        etDept = findViewById(R.id.etDepartment);
        etRoll = findViewById(R.id.etRollNo);

        btnFaculty = findViewById(R.id.btnFaculty);
        btnStudent = findViewById(R.id.btnStudent);
        btnSignup = findViewById(R.id.btnSignup);

        selectRole("STUDENT");

        btnFaculty.setOnClickListener(v -> selectRole("FACULTY"));
        btnStudent.setOnClickListener(v -> selectRole("STUDENT"));

        btnSignup.setOnClickListener(v -> handleSignup());
    }

    // --------------------------------------------------
    // ROLE SELECTION
    // --------------------------------------------------
    private void selectRole(String role) {
        selectedRole = role;

        btnFaculty.setBackgroundResource(
                role.equals("FACULTY") ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected
        );
        btnStudent.setBackgroundResource(
                role.equals("STUDENT") ? R.drawable.bg_role_selected : R.drawable.bg_role_unselected
        );

        etRoll.setVisibility(
                role.equals("STUDENT") ? View.VISIBLE : View.GONE
        );
    }

    // --------------------------------------------------
    // SIGNUP HANDLER
    // --------------------------------------------------
    private void handleSignup() {

        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String name = etName.getText().toString().trim();
        String dept = etDept.getText().toString().trim();
        String roll = etRoll.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty() || name.isEmpty()) {
            Toast.makeText(this, "Required fields missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth.getInstance()
                .createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {

                    String uid = result.getUser().getUid();

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("email", email);
                    userMap.put("role", selectedRole);
                    userMap.put("approved", false);

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .setValue(userMap);

                    if (selectedRole.equals("FACULTY")) {
                        saveFaculty(uid, name, email, dept);
                    } else {
                        saveStudent(uid, name, email, dept, roll);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // --------------------------------------------------
    // SAVE FACULTY (ADMIN APPROVES LATER)
    // --------------------------------------------------
    private void saveFaculty(String uid, String name, String email, String dept) {

        Map<String, Object> faculty = new HashMap<>();
        faculty.put("faculty_id", uid);
        faculty.put("name", name);
        faculty.put("email", email);
        faculty.put("department", dept);
        faculty.put("role", "FACULTY");
        faculty.put("approved", false);

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(uid)
                .setValue(faculty)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Faculty registered. Waiting for admin approval.",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    // --------------------------------------------------
    // SAVE STUDENT (FACULTY APPROVES LATER)
    // --------------------------------------------------
    private void saveStudent(String uid, String name, String email, String dept, String roll) {

        Map<String, Object> student = new HashMap<>();
        student.put("student_id", uid);
        student.put("name", name);
        student.put("email", email);
        student.put("department", dept);
        student.put("roll_no", roll);
        student.put("approved", false);
        student.put("faculty_id", ""); // assigned later

        FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .setValue(student)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Student registered. Waiting for faculty approval.",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
