package com.example.studentrecordfinder.controller;

import com.example.studentrecordfinder.model.Student;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StudentController {

    private final DatabaseReference studentRef =
            FirebaseDatabase.getInstance().getReference("students");

    // --------------------------------------------------
    // GET STUDENT BY UID (STUDENT LOGIN USE)
    // --------------------------------------------------
    public void getStudentByUid(String uid, StudentCallback callback) {

        studentRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Student not found");
                        return;
                    }

                    Student student = snapshot.getValue(Student.class);
                    callback.onSuccess(student);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to load student"));
    }

    // --------------------------------------------------
    // GET STUDENT BY ROLL NO (FACULTY USE)
    // --------------------------------------------------
    public void getStudentByRoll(String rollNo, StudentCallback callback) {

        studentRef.orderByChild("roll_no")
                .equalTo(rollNo)
                .limitToFirst(1)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Student not found");
                        return;
                    }

                    for (DataSnapshot child : snapshot.getChildren()) {
                        Student student = child.getValue(Student.class);
                        callback.onSuccess(student);
                        return;
                    }
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Search failed"));
    }

    // --------------------------------------------------
    // FETCH ALL STUDENTS (ADMIN USE ONLY)
    // --------------------------------------------------
    public void getAllStudents(StudentsCallback callback) {

        studentRef.get()
                .addOnSuccessListener(snapshot -> {

                    List<Student> list = new ArrayList<>();

                    for (DataSnapshot child : snapshot.getChildren()) {
                        Student student = child.getValue(Student.class);
                        if (student != null) {
                            list.add(student);
                        }
                    }

                    callback.onSuccess(list);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to fetch student records"));
    }

    // --------------------------------------------------
    // FETCH MY PROFILE (STUDENT DASHBOARD)
    // --------------------------------------------------
    public void fetchMyProfile(String uid, StudentCallback callback) {

        studentRef.child(uid).get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        callback.onFailure("Student record not found");
                        return;
                    }

                    Student student = snapshot.getValue(Student.class);
                    callback.onSuccess(student);
                })
                .addOnFailureListener(e ->
                        callback.onFailure("Failed to load student data"));
    }

    // --------------------------------------------------
    // CALLBACKS
    // --------------------------------------------------
    public interface StudentCallback {
        void onSuccess(Student student);
        void onFailure(String error);
    }

    public interface StudentsCallback {
        void onSuccess(List<Student> students);
        void onFailure(String error);
    }
}
