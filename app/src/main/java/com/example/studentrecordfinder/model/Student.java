package com.example.studentrecordfinder.model;

import java.util.Map;

public class Student {

    public String student_id;     // auth.uid
    public String name;
    public String roll_no;
    public String department;
    public String email;

    public String faculty_id;     // auth.uid of faculty
    public boolean approved;      // faculty controls

    public Map<String, AttendanceRecord> attendance;
    public Map<String, ResultRecord> marks;

    // REQUIRED empty constructor (Firebase)
    public Student() {}

    public Student(String name, String rollNo) {
        this.name = name;
        this.roll_no = rollNo;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getRollNo() {
        return roll_no != null ? roll_no : "";
    }
}
