package com.example.studentrecordfinder.model;

import java.util.List;

public class Faculty {
    public String faculty_id;
    public String name;
    public String email;
    public String department;
    // Map is safer than List for Firebase "subjects" nodes
    public String role;
    public boolean approved;
    public boolean active = true;


    public Faculty() {} // Keep this!
}
