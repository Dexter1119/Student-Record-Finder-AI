package com.example.studentrecordfinder.controller;

import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.ResultRecord;
import com.example.studentrecordfinder.model.Student;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentAiAnalyzer {

    // ==================================================
    // ALERT GENERATION
    // ==================================================
    public static List<String> generateAlerts(Student student) {

        List<String> alerts = new ArrayList<>();

        // ---------- LOW ATTENDANCE ----------
        if (student.attendance != null) {

            for (Map.Entry<String, AttendanceRecord> entry
                    : student.attendance.entrySet()) {

                String subject = entry.getKey();
                AttendanceRecord record = entry.getValue();

                if (record != null && record.percentage < 75) {
                    alerts.add("Low attendance in "
                            + subject
                            + " ("
                            + record.percentage
                            + "%)");
                }
            }
        }

        // ---------- LOW MARKS ----------
        if (student.marks != null) {

            for (Map.Entry<String, ResultRecord> entry
                    : student.marks.entrySet()) {

                String subject = entry.getKey();
                ResultRecord record = entry.getValue();

                if (record != null && record.total < 40) {
                    alerts.add("Low performance in "
                            + subject
                            + " ("
                            + record.total
                            + " marks)");
                }
            }
        }

        // ---------- NO RISK ----------
        if (alerts.isEmpty()) {
            alerts.add("No critical academic risks detected 🎉");
        }

        return alerts;
    }

    // ==================================================
    // SUGGESTIONS
    // ==================================================
    public static List<String> getSuggestions(Student student) {

        List<String> tips = new ArrayList<>();

        int avgAttendance = 0;
        int count = 0;

        if (student.attendance != null) {
            for (AttendanceRecord r : student.attendance.values()) {
                if (r != null) {
                    avgAttendance += r.percentage;
                    count++;
                }
            }
        }

        if (count > 0) avgAttendance /= count;

        if (avgAttendance < 75) {
            tips.add("Increase attendance in low-performing subjects.");
            tips.add("Meet faculty for attendance improvement plan.");
        }

        if (student.marks != null) {
            tips.add("Focus more on weak subjects.");
            tips.add("Practice previous year question papers.");
            tips.add("Create a weekly revision schedule.");
        }

        tips.add("Maintain consistency and track progress monthly.");

        return tips;
    }
}
