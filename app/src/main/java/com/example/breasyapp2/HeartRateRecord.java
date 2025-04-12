package com.example.breasyapp2;

//used to compile all the BPM and time entries in line charts

public class HeartRateRecord {
    public int time;
    public int bpm;

    public HeartRateRecord() {
        // Needed for Firebase deserialization
    }

    public HeartRateRecord(int time, int bpm) {
        this.time = time;
        this.bpm = bpm;
    }
}
