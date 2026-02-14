package com.example.studentrecordfinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.LoginController;
import com.example.studentrecordfinder.ui.admin.AdminActivity;
import com.example.studentrecordfinder.ui.faculty.FacultyActivity;
import com.example.studentrecordfinder.ui.student.StudentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup;
    private LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginController = new LoginController();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);

        // Existing session check
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            fetchRoleAndNavigate(FirebaseAuth.getInstance().getCurrentUser().getUid());
        }

        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class))
        );

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) return;

            btnLogin.setEnabled(false);

            loginController.login(email, pass, new LoginController.LoginCallback() {
                @Override
                public void onSuccess() {
                    fetchRoleAndNavigate(FirebaseAuth.getInstance().getCurrentUser().getUid());
                }

                @Override
                public void onFailure(String error) {
                    btnLogin.setEnabled(true);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ==================================================
    // FETCH ROLE
    // ==================================================
    private void fetchRoleAndNavigate(String uid) {

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        forceLogout("User data not found");
                        return;
                    }

                    String role = snapshot.getValue(String.class);

                    if ("FACULTY".equalsIgnoreCase(role)) {
                        checkFacultyApproval(uid);
                    } else {
                        navigateToDashboard(role);
                    }
                })
                .addOnFailureListener(e ->
                        forceLogout("Failed to fetch user role"));
    }

    // ==================================================
    // FACULTY APPROVAL + ACTIVE GATE
    // ==================================================
    private void checkFacultyApproval(String uid) {

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(uid)
                .get()
                .addOnSuccessListener(s -> {

                    if (!s.exists()) {
                        forceLogout("Faculty record missing");
                        return;
                    }

                    Boolean approved = s.child("approved").getValue(Boolean.class);
                    Boolean active = s.child("active").getValue(Boolean.class);

                    // Default active = true for old records
                    if (approved != null && approved && (active == null || active)) {
                        navigateToDashboard("FACULTY");
                    } else {
                        forceLogout("Faculty access disabled or pending approval");
                    }
                })
                .addOnFailureListener(e ->
                        forceLogout("Faculty verification failed"));
    }

    // ==================================================
    // DASHBOARD ROUTING
    // ==================================================
    private void navigateToDashboard(String role) {

        Intent intent;

        if ("ADMIN".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminActivity.class);
        } else if ("FACULTY".equalsIgnoreCase(role)) {
            intent = new Intent(this, FacultyActivity.class);
        } else {
            intent = new Intent(this, StudentActivity.class);
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ==================================================
    // FORCE LOGOUT
    // ==================================================
    private void forceLogout(String message) {
        FirebaseAuth.getInstance().signOut();
        btnLogin.setEnabled(true);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
