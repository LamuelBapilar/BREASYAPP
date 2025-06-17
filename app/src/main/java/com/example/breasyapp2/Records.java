package com.example.breasyapp2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.components.YAxis;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Records extends AppCompatActivity {

    private String useremail, userfname, userlname, userbday, useraddress, usergphone, usergname, userdose;
    private TextView greet;
    private Button btnAll, btnToday, btnWeek, btnMonth, btnYear, btn2Y;
    private Button[] filterButtons;
    private LineChart lineChart;
    private ArrayList<Session> allSessions = new ArrayList<>();
    private SessionAdapter adapter;

    //Breathing pattern Guide
    private View breathcircle;
    private AnimatorSet breathingSet;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_records);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        // String Declarations
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE); // retrieve user email from login
        useremail = prefs.getString("useremail", null); // set retrieve email
        userfname = prefs.getString("userfname", "Default Name");
        userlname = prefs.getString("userlname", "Default LastName");
        userbday = prefs.getString("userbday", "Default Date");
        useraddress = prefs.getString("useraddress", "Default Address");
        usergphone = prefs.getString("usergphone", "Default Phone");
        usergname = prefs.getString("usergname", "Default Guardian Name");
        userdose = prefs.getString("userdose", "Default Dose");

        // Button Declarations
        btnAll = findViewById(R.id.buttonAll);
        btnToday = findViewById(R.id.buttonToday);
        btnWeek = findViewById(R.id.buttonWeek);
        btnMonth = findViewById(R.id.buttonMonth);
        btnYear = findViewById(R.id.buttonYear);
        btn2Y = findViewById(R.id.button2Y);
        filterButtons = new Button[]{btnAll, btnToday, btnWeek, btnMonth, btnYear, btn2Y};

        // TextView Declarations
        greet = findViewById(R.id.greetings);
        greet.setText("Hello, " + userfname);
        breathcircle = findViewById(R.id.breathCircle);

        // Line Chart declaration
        lineChart = findViewById(R.id.lineChart);


        // Setup functions
        loadSessionsIntoListView();
        setUpFilterButtons();
        startBreathingAnimation();
    }

    private void loadSessionsIntoListView() {
        DatabaseReference userSessionsRef = FirebaseDatabase.getInstance()
                .getReference("Records")
                .child(userfname + " " + userlname); // or use a more secure user ID

        ArrayList<Session> sessionList = new ArrayList<>();

        userSessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String timestamp = sessionSnap.getKey();
                    int duration = 0;

                    DataSnapshot bpmSnap = sessionSnap.child("bpmRecords");
                    for (DataSnapshot record : bpmSnap.getChildren()) {
                        Integer time = record.child("time").getValue(Integer.class);
                        if (time != null && time > duration) {
                            duration = time;
                        }
                    }

                    sessionList.add(new Session(timestamp, duration));
                }

                // Set up the adapter for the ListView
                SessionAdapter adapter = new SessionAdapter(Records.this, sessionList, userfname + " " + userlname);
                ListView listView = findViewById(R.id.recordview);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Records.this, "Failed to load sessions.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Logout(View view) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().remove("useremail").apply(); // remove saved user email
        prefs.edit().remove("userfname").apply();
        prefs.edit().remove("userlname").apply();
        prefs.edit().remove("userbday").apply();
        prefs.edit().remove("useraddress").apply();
        prefs.edit().remove("usergphone").apply();
        prefs.edit().remove("usergname").apply();
        prefs.edit().remove("userdose").apply();
        Intent intent = new Intent(Records.this, MainActivity.class);
        startActivity(intent);
    } // logout to login page

    public void Records(View view) {

        Intent intent = new Intent(Records.this, Records.class);
        startActivity(intent);

    } // goto user's record page

    public void Profile(View view){
        Intent intent = new Intent(this, Profilepage.class);
        startActivity(intent);
    } // goto user's Profile page

    public void Home(View view) {

        Intent intent = new Intent(Records.this, Homepage.class);
        startActivity(intent);

    } // goto user's record page

    public void loadSessionDataToLineChart(String userKey, String sessionKey) {
        DatabaseReference bpmRecordsRef = FirebaseDatabase.getInstance()
                .getReference("Records")
                .child(userKey)
                .child(sessionKey)
                .child("bpmRecords"); // 👈 Only access bpmRecords

        bpmRecordsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                ArrayList<Entry> chartEntries = new ArrayList<>();

                for (DataSnapshot record : snapshot.getChildren()) {
                    Integer time = record.child("time").getValue(Integer.class);
                    Integer bpm = record.child("bpm").getValue(Integer.class);

                    if (time != null && bpm != null) {
                        chartEntries.add(new Entry(time, bpm));
                    }
                }

                updateLineChartWithEntries(chartEntries); // ✅ Update chart
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLineChartWithEntries(ArrayList<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(0f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setDrawCircles(false);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh chart
    }

    private void setUpFilterButtons() {
        for (Button button : filterButtons) {
            button.setOnClickListener(v -> {
                resetFilterButtonStyles();
                setActiveFilterButton((Button) v);

                String selectedFilter = ((Button) v).getText().toString();
                filterByRange(selectedFilter);
            });
        }

        // Set default to All
        setActiveFilterButton(btnAll);
        filterByRange("All");
    }

    private void resetFilterButtonStyles() {
        for (Button btn : filterButtons) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#DDDDDD")));
            btn.setTextColor(Color.parseColor("#666666"));
        }
    }

    private void setActiveFilterButton(Button button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6DD19C")));
        button.setTextColor(Color.WHITE);
    }

    private void filterByRange(String range) {
        final long now = System.currentTimeMillis();
        final long cutoff;

        switch (range) {
            case "Today":
                cutoff = now - TimeUnit.DAYS.toMillis(1);
                break;
            case "Week":
                cutoff = now - TimeUnit.DAYS.toMillis(7);
                break;
            case "Month":
                cutoff = now - TimeUnit.DAYS.toMillis(30);
                break;
            case "Year":
                cutoff = now - TimeUnit.DAYS.toMillis(365);
                break;
            case "2Y":
                cutoff = now - TimeUnit.DAYS.toMillis(730);
                break;
            case "All":
            default:
                cutoff = 0;
                break;
        }

        DatabaseReference userSessionsRef = FirebaseDatabase.getInstance()
                .getReference("Records")
                .child(userfname + " " + userlname); // can replace with UID for security

        ArrayList<Session> filteredSessionList = new ArrayList<>();
        userSessionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String timestamp = sessionSnap.getKey();
                    long sessionTimestamp = 0;

                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = sdf.parse(timestamp);
                        if (date != null) {
                            sessionTimestamp = date.getTime();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    if (sessionTimestamp >= cutoff) {
                        int duration = 0;
                        DataSnapshot bpmSnap = sessionSnap.child("bpmRecords");
                        for (DataSnapshot record : bpmSnap.getChildren()) {
                            Integer time = record.child("time").getValue(Integer.class);
                            if (time != null && time > duration) {
                                duration = time;
                            }
                        }

                        filteredSessionList.add(new Session(timestamp, duration));
                    }
                }

                // Sort latest first
                Collections.sort(filteredSessionList, (s1, s2) -> {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date d1 = sdf.parse(s1.getTimestamp());
                        Date d2 = sdf.parse(s2.getTimestamp());
                        return d2.compareTo(d1); // Descending
                    } catch (ParseException e) {
                        return 0;
                    }
                });

                // Set adapter
                SessionAdapter adapter = new SessionAdapter(Records.this, filteredSessionList, userfname + " " + userlname);
                ListView listView = findViewById(R.id.recordview);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Records.this, "Failed to filter sessions.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startBreathingAnimation() {
        // Inhale animation (scale up)
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(breathcircle, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(breathcircle, "scaleY", 1f, 1.4f);
        scaleUpX.setDuration(3000);
        scaleUpY.setDuration(3000);

        // Exhale animation (scale down)
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(breathcircle, "scaleX", 1.4f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(breathcircle, "scaleY", 1.4f, 1f);
        scaleDownX.setDuration(4000);
        scaleDownY.setDuration(4000);

        // Set text to "Inhale" at the start of scale-up animation
        scaleUpX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });

        // Set text to "Exhale" at the start of scale-down animation
        scaleDownX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });

        // Group scale-up animations
        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.playTogether(scaleUpX, scaleUpY);

        // Group scale-down animations
        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.playTogether(scaleDownX, scaleDownY);

        // Full breathing animation set (inhale then exhale)
        breathingSet = new AnimatorSet();
        breathingSet.playSequentially(scaleUp, scaleDown);  // Play scale up, then scale down
        breathingSet.setStartDelay(500); // Small pause before start

        // Loop the animation manually when it ends
        breathingSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (breathingSet != null) {
                    breathingSet.start(); // Restart the animation loop
                }
            }
        });

        // Start the breathing animation
        breathingSet.start();
    } // start breathing guide circle


}