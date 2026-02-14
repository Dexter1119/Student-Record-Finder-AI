package com.example.studentrecordfinder.controller;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

public class LoginController {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // ==================================================
    // LOGIN WITH APPROVAL GATE
    // ==================================================
    public void login(String email, String password, LoginCallback callback) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = auth.getCurrentUser().getUid();

                    // 🔐 Fetch role first
                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .get()
                            .addOnSuccessListener(userSnap -> {

                                if (!userSnap.exists()) {
                                    callback.onFailure("User data missing");
                                    return;
                                }

                                String role = userSnap.child("role").getValue(String.class);

                                if ("ADMIN".equalsIgnoreCase(role)) {
                                    // Admin has no approval gate
                                    callback.onSuccess();
                                    return;
                                }

                                // 🔍 Check approval for FACULTY / STUDENT
                                String node =
                                        "FACULTY".equalsIgnoreCase(role)
                                                ? "faculty"
                                                : "students";

                                FirebaseDatabase.getInstance()
                                        .getReference(node)
                                        .child(uid)
                                        .child("approved")
                                        .get()
                                        .addOnSuccessListener(approvedSnap -> {

                                            Boolean approved = approvedSnap.getValue(Boolean.class);

                                            if (approved != null && approved) {
                                                callback.onSuccess();
                                            } else {
                                                auth.signOut();
                                                callback.onFailure("Account pending approval");
                                            }
                                        });
                            });
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Invalid email or password")
                );
    }

    // ==================================================
    // REGISTER (NO APPROVAL CHECK HERE)
    // ==================================================
    public void register(String email, String password, String role, LoginCallback callback) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {

                    String uid = auth.getCurrentUser().getUid();

                    FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(uid)
                            .setValue(new UserRole(email, role.toUpperCase()))
                            .addOnSuccessListener(v -> callback.onSuccess())
                            .addOnFailureListener(e ->
                                    callback.onFailure("Database error")
                            );
                })
                .addOnFailureListener(e ->
                        callback.onFailure(e.getMessage())
                );
    }

    // ==================================================
    // CALLBACK
    // ==================================================
    public interface LoginCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // ==================================================
    // SIMPLE ROLE HOLDER
    // ==================================================
    static class UserRole {
        public String email;
        public String role;

        public UserRole() {}

        UserRole(String email, String role) {
            this.email = email;
            this.role = role;
        }
    }
}
