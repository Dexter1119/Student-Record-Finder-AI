package com.example.studentrecordfinder.controller;

import com.google.firebase.database.*;

import java.util.HashMap;
import java.util.Map;

public class AttendanceController {

    private final DatabaseReference ref =
            FirebaseDatabase.getInstance().getReference("students");

    // --------------------------------------------------
    // UPDATE ATTENDANCE BY ROLL (PERCENTAGE BASED)
    // --------------------------------------------------
    public void updateAttendanceByRoll(
            String roll,
            String subject,
            int percentage,
            Callback callback
    ) {

        ref.orderByChild("roll_no")
                .equalTo(roll)
                .limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Student not found");
                        return;
                    }

                    for (DataSnapshot s : snapshot.getChildren()) {

                        Map<String, Object> data = new HashMap<>();
                        data.put("percentage", percentage);

                        s.getRef()
                                .child("attendance")
                                .child(subject)
                                .setValue(data)
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(e ->
                                        callback.onFailure("Attendance update failed"));
                        return;
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Database error"));
    }
    public static void update(String studentId, String subject, int attended, int total) {

        int percentage = (attended * 100) / total;

        FirebaseDatabase.getInstance()
                .getReference("students")
                .child(studentId)
                .child("attendance")
                .child(subject)
                .setValue(
                        Map.of(
                                "attended", attended,
                                "total_classes", total,
                                "percentage", percentage
                        )
                );
    }

    // --------------------------------------------------
    // CALLBACK
    // --------------------------------------------------
    public interface Callback {
        void onSuccess();
        void onFailure(String error);
    }
}
