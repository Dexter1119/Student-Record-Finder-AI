package com.example.studentrecordfinder.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.ui.MainActivity;
import com.example.studentrecordfinder.ui.admin.dashboard.AdminDashboardFragment;
import com.example.studentrecordfinder.ui.admin.students.AdminStudentsFragment;
import com.example.studentrecordfinder.ui.admin.faculty.AdminFacultyFragment;
import com.example.studentrecordfinder.ui.admin.ai.AdminChatFragment;
import com.example.studentrecordfinder.ui.admin.more.AdminMoreFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AdminActivity extends AppCompatActivity {

    private int currentMenuId = R.id.nav_admin_dashboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 HARD SESSION CHECK
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            redirectToLogin();
            return;
        }

        setContentView(R.layout.activity_admin);

        verifyAdminRole();
        setupBottomNavigation();
        setupLogout();
        blockBackNavigation();

        // DEFAULT SCREEN
        loadFragment(new AdminDashboardFragment());
    }

    // ==================================================
    // ROLE VERIFICATION
    // ==================================================
    private void verifyAdminRole() {

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .child("role")
                .get()
                .addOnSuccessListener(snapshot -> {

                    String role = snapshot.getValue(String.class);
                    if (!"ADMIN".equalsIgnoreCase(role)) {
                        forceLogout("Unauthorized access");
                    }
                })
                .addOnFailureListener(e ->
                        forceLogout("Access verification failed"));
    }

    // ==================================================
    // BOTTOM NAVIGATION
    // ==================================================
    private void setupBottomNavigation() {

        BottomNavigationView nav = findViewById(R.id.bottomNavAdmin);

        nav.setOnItemSelectedListener(item -> {

            int id = item.getItemId();
            if (id == currentMenuId) return true;
            currentMenuId = id;

            if (id == R.id.nav_admin_dashboard) {
                loadFragment(new AdminDashboardFragment());
            }
            else if (id == R.id.nav_admin_students) {
                loadFragment(new AdminStudentsFragment());
            }
            else if (id == R.id.nav_admin_faculty) {
                loadFragment(new AdminFacultyFragment());
            }
            else if (id == R.id.nav_admin_ai) {
                loadFragment(new AdminChatFragment());
            }
//            else if (id == R.id.nav_admin_more) {
//                loadFragment(new AdminMoreFragment());
//            }

            return true;
        });
    }

    // ==================================================
    // FRAGMENT LOADER
    // ==================================================
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .commit();
    }

    // ==================================================
    // LOGOUT
    // ==================================================
    private void setupLogout() {
        ImageButton btnLogout = findViewById(R.id.btnAdminLogout);
        btnLogout.setOnClickListener(v -> forceLogout("Logged out"));
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

    // ==================================================
    // BLOCK BACK
    // ==================================================
    private void blockBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        // Root level – block back
                    }
                });
    }
}
