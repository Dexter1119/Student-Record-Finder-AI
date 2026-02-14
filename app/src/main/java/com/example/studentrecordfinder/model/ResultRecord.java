package com.example.studentrecordfinder.model;

public class ResultRecord {
    public int internal;
    public int external;
    public int total;

    public ResultRecord() {}

    public ResultRecord(int internal, int external) {
        this.internal = internal;
        this.external = external;
        this.total = internal + external;
    }
}