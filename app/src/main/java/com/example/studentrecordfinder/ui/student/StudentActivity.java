package com.example.studentrecordfinder.ui.student;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.ui.MainActivity;
import com.example.studentrecordfinder.ui.student.fragments.StudentAiFragment;
import com.example.studentrecordfinder.ui.student.fragments.StudentDashboardFragment;
import com.example.studentrecordfinder.ui.student.fragments.StudentResultsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class StudentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 SESSION CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_student);

        ImageButton btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(v -> forceLogout("Logged out"));

        lockBackNavigation();
        verifyStudentRole();   // ✅ gatekeeper
    }

    // ---------------- ROLE VERIFICATION ----------------
    private void verifyStudentRole() {

        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener(snapshot -> {

                    String role = snapshot.getValue(String.class);

                    if (role == null || !"STUDENT".equalsIgnoreCase(role)) {
                        forceLogout("Unauthorized access");
                        return;
                    }

                    // ✅ Only load UI AFTER role is verified
                    setupBottomNavigation();
                })
                .addOnFailureListener(e ->
                        forceLogout("Access verification failed")
                );
    }

    // ---------------- NAVIGATION ----------------
    private void setupBottomNavigation() {

        BottomNavigationView bottomNav = findViewById(R.id.studentBottomNav);

        // Default fragment
        loadFragment(new StudentDashboardFragment());

        bottomNav.setOnItemSelectedListener(item -> {

            Fragment fragment;

            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                fragment = new StudentDashboardFragment();
            } else if (id == R.id.nav_results) {
                fragment = new StudentResultsFragment();
            } else {
                fragment = new StudentAiFragment();
            }

            loadFragment(fragment);
            return true;
        });
    }

    // ✅ FIXED METHOD (GENERIC FRAGMENT)
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.studentFragmentContainer, fragment)
                .commit();
    }

    // ---------------- SECURITY ----------------
    private void lockBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Disabled
                    }
                });
    }

    private void forceLogout(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        redirectToLogin();
    }

    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
