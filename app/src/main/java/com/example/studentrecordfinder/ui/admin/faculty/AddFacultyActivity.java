package com.example.studentrecordfinder.ui.admin.faculty;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.Faculty;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddFacultyActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etDept;
    private Button btnCreate;

    private FirebaseAuth facultyAuth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_faculty);

        db = FirebaseDatabase.getInstance();
        initSecondaryAuth();

        etName = findViewById(R.id.etFacultyName);
        etEmail = findViewById(R.id.etFacultyEmail);
        etPassword = findViewById(R.id.etFacultyPassword);
        etDept = findViewById(R.id.etFacultyDept);
        btnCreate = findViewById(R.id.btnCreateFaculty);

        btnCreate.setOnClickListener(v -> createFaculty());
    }

    // ==================================================
    // SECONDARY AUTH (SAFE INIT)
    // ==================================================
    private void initSecondaryAuth() {
        FirebaseOptions options = FirebaseOptions.fromResource(this);
        FirebaseApp app;

        try {
            app = FirebaseApp.initializeApp(this, options, "FacultyCreation");
        } catch (IllegalStateException e) {
            app = FirebaseApp.getInstance("FacultyCreation");
        }

        facultyAuth = FirebaseAuth.getInstance(app);
    }

    // ==================================================
    // CREATE FACULTY (ADMIN → FACULTY)
    // ==================================================
    private void createFaculty() {

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String dept = etDept.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || dept.isEmpty() || pass.length() < 6) {
            Toast.makeText(
                    this,
                    "All fields required (password ≥ 6 chars)",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        btnCreate.setEnabled(false);

        facultyAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {

                    String facultyUid = result.getUser().getUid();

                    // ---------- FACULTY TABLE ----------
                    Faculty faculty = new Faculty();
                    faculty.faculty_id = facultyUid;
                    faculty.name = name;
                    faculty.email = email;
                    faculty.department = dept;
                    faculty.role = "FACULTY";
                    faculty.approved = false;

                    db.getReference("faculty")
                            .child(facultyUid)
                            .setValue(faculty)
                            .addOnSuccessListener(v -> {

                                // ---------- USERS TABLE ----------
                                Map<String, Object> userMap = new HashMap<>();
                                userMap.put("role", "FACULTY");
                                userMap.put("email", email);
                                userMap.put("approved", false);

                                db.getReference("users")
                                        .child(facultyUid)
                                        .setValue(userMap)
                                        .addOnSuccessListener(u -> {

                                            // 🔥 CRITICAL FIX
                                            facultyAuth.signOut();

                                            Toast.makeText(
                                                    this,
                                                    "Faculty account created successfully.\nAwaiting admin approval.",
                                                    Toast.LENGTH_LONG
                                            ).show();

                                            finish();
                                        });
                            });
                })
                .addOnFailureListener(e -> {
                    btnCreate.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
