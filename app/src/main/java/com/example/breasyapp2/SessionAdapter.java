package com.example.breasyapp2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SessionAdapter extends ArrayAdapter<Session> {

    private String userIdentifier; // Store the user identifier (could be userfname + " " + userlname)

    public SessionAdapter(@NonNull Context context, ArrayList<Session> sessions, String userIdentifier) {
        super(context, 0, sessions);
        this.userIdentifier = userIdentifier; // Initialize the user identifier
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.session_item, parent, false);
        }

        Session currentSession = getItem(position);

        TextView timestampTextView = listItemView.findViewById(R.id.timestampText);
        TextView durationTextView = listItemView.findViewById(R.id.durationText);
        Button viewButton = listItemView.findViewById(R.id.viewButton);
        Button deleteButton = listItemView.findViewById(R.id.deleteButton);

        timestampTextView.setText(currentSession.getTimestamp());
        durationTextView.setText("Duration: " + currentSession.getDuration() + " seconds");

        viewButton.setOnClickListener(v -> {
            // Code to view session details (e.g., load data to line chart)
            loadSessionDataToLineChart(currentSession.getTimestamp());
        });

        deleteButton.setOnClickListener(v -> {
            // Code to delete session
            deleteSessionFromFirebase(currentSession.getTimestamp(), position);
        });

        return listItemView;
    }

    private void loadSessionDataToLineChart(String timestamp) {
        // You can modify this code to show detailed session data on a line chart or another view
        ((Records) getContext()).loadSessionDataToLineChart(userIdentifier, timestamp);
    }

    private void deleteSessionFromFirebase(String timestamp, int position) {
        // Get reference to Firebase database, using the dynamic user identifier (userfname + " " + userlname)
        DatabaseReference sessionRef = FirebaseDatabase.getInstance()
                .getReference("Records")
                .child(userIdentifier) // Use actual user identifier (userfname + " " + userlname)
                .child(timestamp); // Reference to specific session using timestamp

        sessionRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the session from the adapter and update the ListView
                remove(getItem(position));
                notifyDataSetChanged();
                Toast.makeText(getContext(), "Session deleted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to delete session", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
