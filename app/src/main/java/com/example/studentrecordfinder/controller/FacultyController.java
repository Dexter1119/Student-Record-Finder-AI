package com.example.studentrecordfinder.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FacultyController {

    // ==================================================
    // REFERENCES
    // ==================================================
    private final DatabaseReference facultyRef =
            FirebaseDatabase.getInstance().getReference("faculty");

    private final DatabaseReference studentRef =
            FirebaseDatabase.getInstance().getReference("students");

    // ==================================================
    // FETCH FACULTY PROFILE (SELF)
    // ==================================================
    public void fetchFacultyProfile(String uid, FacultyCallback callback) {

        facultyRef
                .child(uid)
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to load faculty profile")
                );
    }

    // ==================================================
    // FETCH ONLY MY STUDENTS (OWNERSHIP ENFORCED)
    // ==================================================
    public void fetchAllStudents(FacultyCallback callback) {

        String facultyUid = FirebaseAuth.getInstance().getUid();

        if (facultyUid == null) {
            callback.onFailure("Faculty not authenticated");
            return;
        }

        studentRef
                .orderByChild("faculty_id")
                .equalTo(facultyUid)   // 🔐 faculty_id === auth.uid
                .get()
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to load students")
                );
    }

    // ==================================================
    // CALLBACK
    // ==================================================
    public interface FacultyCallback {
        void onSuccess(DataSnapshot snapshot);
        void onFailure(String error);
    }
}
