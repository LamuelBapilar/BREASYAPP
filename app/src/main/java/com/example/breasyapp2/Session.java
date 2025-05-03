package com.example.breasyapp2;

public class Session {
    private String timestamp;
    private String duration;

    // Constructor for the session
    public Session(String timestamp, int durationInSeconds) {
        this.timestamp = timestamp;
        this.duration = convertDurationToString(durationInSeconds);
    }

    // Convert duration from seconds to MM:SS format
    private String convertDurationToString(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getDuration() {
        return duration;
    }

    // Optionally, you can add setters if needed
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setDuration(int durationInSeconds) {
        this.duration = convertDurationToString(durationInSeconds);
    }
}
