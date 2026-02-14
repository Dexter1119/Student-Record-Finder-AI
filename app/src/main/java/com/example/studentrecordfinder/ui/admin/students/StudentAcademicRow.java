package com.example.studentrecordfinder.ui.admin.students;

public class StudentAcademicRow {

    public String subject;
    public int attendancePercent;
    public int internal;
    public int external;
    public int total;

    public StudentAcademicRow(
            String subject,
            int attendancePercent,
            int internal,
            int external,
            int total
    ) {
        this.subject = subject;
        this.attendancePercent = attendancePercent;
        this.internal = internal;
        this.external = external;
        this.total = total;
    }
}
