package com.example.studentrecordfinder.ui.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.ui.MainActivity;
import com.example.studentrecordfinder.ui.faculty.alerts.FacultyAlertsFragment;
import com.example.studentrecordfinder.ui.faculty.chat.FacultyChatFragment;
import com.example.studentrecordfinder.ui.faculty.dashboard.FacultyDashboardFragment;
import com.example.studentrecordfinder.ui.faculty.records.FacultyRecordsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FacultyActivity extends AppCompatActivity {

    private int currentMenuId = R.id.nav_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty);

        verifyFacultyRole();
        blockBackNavigation();
        setupBottomNavigation();     // MUST remain unchanged
        setupLogout();

        // Default screen
        loadFragment(new FacultyDashboardFragment());
    }

    // --------------------------------------------------
    // ROLE VERIFICATION (RBAC)
    // --------------------------------------------------
    private void verifyFacultyRole() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            forceLogout("Session expired");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String role = snapshot.getValue(String.class);
                    if (!"FACULTY".equalsIgnoreCase(role)) {
                        forceLogout("Unauthorized access");
                    }
                })
                .addOnFailureListener(e ->
                        forceLogout("Access verification failed"));
    }

    // --------------------------------------------------
    // BOTTOM NAVIGATION (DO NOT MODIFY)
    // --------------------------------------------------
    private void setupBottomNavigation() {

        BottomNavigationView nav = findViewById(R.id.bottomNavFaculty);

        nav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == currentMenuId) return true;

            currentMenuId = id;

            if (id == R.id.nav_dashboard) {
                loadFragment(new FacultyDashboardFragment());

            } else if (id == R.id.nav_records) {
                loadFragment(new FacultyRecordsFragment());

            } else if (id == R.id.nav_chat) {
                loadFragment(new FacultyChatFragment());

            } else if (id == R.id.nav_alerts) {
                loadFragment(new FacultyAlertsFragment());
            }

            return true;
        });
    }

    // --------------------------------------------------
    // FRAGMENT LOADER
    // --------------------------------------------------
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.facultyFragmentContainer, fragment)
                .commit();
    }

    // --------------------------------------------------
    // LOGOUT
    // --------------------------------------------------
    private void setupLogout() {
        ImageButton btnLogout = findViewById(R.id.btnFacultyLogout);
        btnLogout.setOnClickListener(v -> forceLogout("Logged out"));
    }

    // --------------------------------------------------
    // BACK NAVIGATION BLOCK
    // --------------------------------------------------
    private void blockBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // intentionally blocked
                    }
                });
    }

    // --------------------------------------------------
    // FORCE LOGOUT
    // --------------------------------------------------
    private void forceLogout(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        redirectToLogin();
    }

    // --------------------------------------------------
    // REDIRECT
    // --------------------------------------------------
    private void redirectToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
