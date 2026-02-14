package com.example.studentrecordfinder.controller;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AdminController {

    // ==================================================
    // RAW FETCH (LOW LEVEL ACCESS)
    // ==================================================
    public void fetchAllData(AdminCallback cb) {

        FirebaseDatabase.getInstance()
                .getReference()
                .get()
                .addOnSuccessListener(cb::onSuccess)
                .addOnFailureListener(e ->
                        cb.onFailure(e.getMessage()));
    }

    // ==================================================
    // GLOBAL CONTEXT BUILDER (ADMIN AI)
    // ==================================================
    public void buildGlobalContext(ContextCallback callback) {

        fetchAllData(new AdminCallback() {

            @Override
            public void onSuccess(DataSnapshot root) {

                StringBuilder ctx = new StringBuilder();

                // ================= USERS =================
                ctx.append("=== USERS ===\n");
                for (DataSnapshot u : root.child("users").getChildren()) {
                    ctx.append("- UID: ").append(u.getKey())
                            .append(", Role: ")
                            .append(u.child("role").getValue(String.class))
                            .append("\n");
                }

                // ================= FACULTY =================
                ctx.append("\n=== FACULTY & STUDENTS ===\n");

                for (DataSnapshot facultySnap :
                        root.child("faculty").getChildren()) {

                    String fid = facultySnap.getKey();
                    String fname = facultySnap.child("name")
                            .getValue(String.class);
                    String dept = facultySnap.child("department")
                            .getValue(String.class);
                    Boolean approved = facultySnap.child("approved")
                            .getValue(Boolean.class);

                    ctx.append("\nFaculty: ")
                            .append(fname)
                            .append(" | Dept: ").append(dept)
                            .append(" | Approved: ").append(approved)
                            .append("\n");

                    // ================= STUDENTS =================
                    for (DataSnapshot studentSnap :
                            root.child("students").getChildren()) {

                        String owner =
                                studentSnap.child("faculty_id")
                                        .getValue(String.class);

                        if (!fid.equals(owner)) continue;

                        String sname =
                                studentSnap.child("name")
                                        .getValue(String.class);

                        String roll =
                                studentSnap.child("roll_no")
                                        .getValue(String.class);

                        ctx.append("  Student: ")
                                .append(sname)
                                .append(" (Roll ")
                                .append(roll)
                                .append(")\n");

                        // -------- ATTENDANCE --------
                        ctx.append("    Attendance:\n");
                        for (DataSnapshot sub :
                                studentSnap.child("attendance").getChildren()) {

                            Integer perc =
                                    sub.child("percentage")
                                            .getValue(Integer.class);

                            ctx.append("      ")
                                    .append(sub.getKey())
                                    .append(": ")
                                    .append(perc)
                                    .append("%\n");
                        }

                        // -------- RESULTS --------
                        ctx.append("    Results:\n");
                        for (DataSnapshot sub :
                                studentSnap.child("marks").getChildren()) {

                            Integer total =
                                    sub.child("total")
                                            .getValue(Integer.class);

                            ctx.append("      ")
                                    .append(sub.getKey())
                                    .append(": ")
                                    .append(total)
                                    .append("\n");
                        }
                    }
                }

                callback.onReady(ctx.toString());
            }

            @Override
            public void onFailure(String error) {
                callback.onReady(
                        "⚠ Unable to load global academic context due to data error."
                );
            }
        });
    }
    // ==================================================
// DELETE FACULTY (ADMIN ONLY)
// ==================================================
    public void deleteFaculty(
            String facultyUid,
            DeleteCallback callback
    ) {

        DatabaseReference root =
                FirebaseDatabase.getInstance().getReference();

        root.child("faculty")
                .child(facultyUid)
                .removeValue()
                .addOnSuccessListener(unused -> {

                    // Also remove from users node
                    root.child("users")
                            .child(facultyUid)
                            .removeValue()
                            .addOnSuccessListener(v ->
                                    callback.onSuccess()
                            )
                            .addOnFailureListener(e ->
                                    callback.onFailure(e.getMessage())
                            );

                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }

    // ==================================================
    public interface DeleteCallback {
        void onSuccess();
        void onFailure(String error);
    }


    // ==================================================
    // CALLBACKS
    // ==================================================
    public interface AdminCallback {
        void onSuccess(DataSnapshot snapshot);
        void onFailure(String error);
    }

    public interface ContextCallback {
        void onReady(String context);
    }
}
