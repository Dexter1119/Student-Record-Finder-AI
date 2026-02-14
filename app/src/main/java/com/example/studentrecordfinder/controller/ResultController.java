package com.example.studentrecordfinder.controller;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class ResultController {

    private static final DatabaseReference ref =
            FirebaseDatabase.getInstance().getReference("students");

    // ----------------------------------
    // UPDATE RESULT BY STUDENT ID
    // ----------------------------------
    public static void update(
            String studentId,
            String subject,
            int internal,
            int external
    ) {

        int total = internal + external;

        Map<String, Object> data = new HashMap<>();
        data.put("internal", internal);
        data.put("external", external);
        data.put("total", total);


        ref.child(studentId)
                .child("marks")
                .child(subject)
                .setValue(data);
    }

    // ----------------------------------
    // OPTIONAL: UPDATE BY ROLL (ADMIN USE)
    // ----------------------------------
    public void updateResultByRoll(
            String roll,
            String subject,
            int internal,
            int external,
            Callback callback
    ) {

        int total = internal + external;

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
                        data.put("internal", internal);
                        data.put("external", external);
                        data.put("total", total);


                        s.getRef()
                                .child("marks")
                                .child(subject)
                                .setValue(data)
                                .addOnSuccessListener(v -> callback.onSuccess())
                                .addOnFailureListener(e ->
                                        callback.onFailure("Result update failed"));
                        break;
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Query failed"));
    }

    // ----------------------------------
    // CALLBACK
    // ----------------------------------
    public interface Callback {
        void onSuccess();
        void onFailure(String error);
    }
}
