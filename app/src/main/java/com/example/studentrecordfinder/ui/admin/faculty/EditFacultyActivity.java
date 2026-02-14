package com.example.studentrecordfinder.ui.admin.faculty;

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.studentrecordfinder.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditFacultyActivity extends AppCompatActivity {

    private EditText etName, etDept;
    private Button btnSave, btnDisable;

    private String facultyUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_faculty);

        facultyUid = getIntent().getStringExtra("faculty_uid");
        if (facultyUid == null) {
            Toast.makeText(this, "Invalid faculty", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etName = findViewById(R.id.etFacultyName);
        etDept = findViewById(R.id.etFacultyDept);
        btnSave = findViewById(R.id.btnSaveFaculty);
        btnDisable = findViewById(R.id.btnDisableFaculty);

        loadFaculty();

        btnSave.setOnClickListener(v -> updateFaculty());
        btnDisable.setOnClickListener(v -> disableFaculty());
    }

    // ==================================================
    // LOAD EXISTING DATA
    // ==================================================
    private void loadFaculty() {
        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(facultyUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) return;

                    etName.setText(snapshot.child("name").getValue(String.class));
                    etDept.setText(snapshot.child("department").getValue(String.class));
                });
    }

    // ==================================================
    // UPDATE FACULTY DETAILS
    // ==================================================
    private void updateFaculty() {

        String name = etName.getText().toString().trim();
        String dept = etDept.getText().toString().trim();

        if (name.isEmpty() || dept.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("department", dept);

        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(facultyUid)
                .updateChildren(updates)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "Faculty updated", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    // ==================================================
    // DISABLE FACULTY (SOFT DELETE)
    // ==================================================
    private void disableFaculty() {
        FirebaseDatabase.getInstance()
                .getReference("faculty")
                .child(facultyUid)
                .child("active")
                .setValue(false)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this,
                            "Faculty access disabled",
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }
}
