package com.example.studentrecordfinder.model;
import com.google.firebase.database.IgnoreExtraProperties;

public class AttendanceRecord {
    public int attended;
    public int total;
    public int percentage;


    public AttendanceRecord() {}

    public AttendanceRecord(int attended, int total_classes) {
        this.attended = attended;
        this.total =total_classes;
        this.percentage = (total_classes > 0) ? (attended * 100 / total_classes) : 0;
    }
}