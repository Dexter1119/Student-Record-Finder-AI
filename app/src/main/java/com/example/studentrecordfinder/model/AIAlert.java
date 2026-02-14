package com.example.studentrecordfinder.model;

public class AIAlert {

    public String title;
    public String description;
    public String severity; // LOW, MEDIUM, HIGH

    public AIAlert() {
        // Required for Firebase / adapters
    }

    public AIAlert(String title, String description, String severity) {
        this.title = title;
        this.description = description;
        this.severity = severity;
    }
}
