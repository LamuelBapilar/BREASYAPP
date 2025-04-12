package com.example.breasyapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
//firebase
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.example.breasyapp2.HeartRateRecord;

public class Homepage extends AppCompatActivity {

    EditText editText, editphone;
    TextView Battery, BPM, Oxygen, Session, greetings;
    ImageView Bat_view, BPM_view, Oxy_view, Ses_view;
    String useremail, userfname, userlname, userbday, useraddress, usergphone, usergname;
    int randomBPM, randomOxygen;
    boolean isRecordingBPM = false;
    private Handler heartRateHandler;
    private Runnable heartRateRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_homepage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;

        });

        //String Declarations
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE); // retrieve user email from login
        useremail = prefs.getString("useremail", null); // set retrieve email
        //// TextView Declarations
        BPM = findViewById(R.id.BPM);
        Oxygen = findViewById(R.id.Oxygen);
        Session = findViewById(R.id.Session);
        greetings = findViewById(R.id.name_greetings);
        //starter function
        demostats(); //for demo
        retrieveuserdata(); //retrieve user data
        handleLineGraph();
        startHeartRateRecording();
    }

    //Heartbeat Line graph dependencies
    ArrayList<Entry> entries = new ArrayList<>();
    int timeCounter = 0; // Time in seconds (e.g., 0, 30, 60, ...

    public void startHeartRateRecording() {
        isRecordingBPM = true;
        heartRateHandler = new Handler();

        heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRecordingBPM) return; // Prevents future runs if stopped

                // Simulated heart rate value (replace with real data)
                int HeartRate = randomBPM;

                // Add new entry
                entries.add(new Entry(timeCounter, HeartRate));
                timeCounter += 1;

                // Refresh graph
                updateLineChart();

                // Continue every 30 seconds
                heartRateHandler.postDelayed(this, 1000); // change to 30000 for real BPM timing
            }
        };

        heartRateHandler.post(heartRateRunnable);
    } // to start BPM monitor

    public void stopHeartRateRecording(View view){
        isRecordingBPM = false;
        heartRateHandler.removeCallbacks(heartRateRunnable);
    }; // to stop BPM monitor

    public void saveSession(View view) {
        DatabaseReference recordsRef = FirebaseDatabase.getInstance().getReference("Records");

        String sessionKey = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        String userchild = userfname + " " + userlname;

        List<HeartRateRecord> sessionRecords = new ArrayList<>();
        for (Entry entry : entries) {
            int time = (int) entry.getX();
            int bpm = (int) entry.getY();
            sessionRecords.add(new HeartRateRecord(time, bpm));
        }

        if (isRecordingBPM) {
            Toast.makeText(this, "Session on progress", Toast.LENGTH_SHORT).show();
        } else {
            recordsRef.child(userchild).child(sessionKey).setValue(sessionRecords);

            Toast.makeText(this, "Session saved to Firebase", Toast.LENGTH_SHORT).show();
        }

    } // to save BPM monitor

    private void updateLineChart() {
        LineChart lineChart = findViewById(R.id.lineChart);
        LineDataSet lineDataSet = new LineDataSet(entries, "Heart Rate (BPM)");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);

        // Graphic settings of the line graph

        lineDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleColor(getResources().getColor(android.R.color.holo_green_light));
        lineDataSet.setCircleRadius(2f);
        lineDataSet.setValueTextSize(8f);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "s";
            }
        });

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(50f);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);
        rightAxis.setDrawAxisLine(true);
        rightAxis.setDrawLabels(false);
        rightAxis.setDrawGridLines(false);
        rightAxis.setAxisLineColor(Color.GRAY);
        rightAxis.setAxisLineWidth(1f);

        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);


        lineChart.invalidate(); //update the line graph uwu
        lineChart.moveViewToX(timeCounter); //follow the latest
        lineChart.setVisibleXRangeMaximum(10); //show only the last 10 seconds
    } // update line chart

    public void Logout(View view){
        Intent intent = new Intent(Homepage.this, MainActivity.class);
        startActivity(intent);
    } // logout to login page

    public void Records(View view){
        Intent intent = new Intent(Homepage.this, Records.class);
        startActivity(intent);
    } // goto user's record page

    public void retrieveuserdata() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users");

        ref.orderByChild("email").equalTo(useremail).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DataSnapshot userSnapshot = snapshot.getChildren().iterator().next(); // Get first match

                    userfname = userSnapshot.child("firstName").getValue(String.class);
                    userlname = userSnapshot.child("lastName").getValue(String.class);
                    useraddress = userSnapshot.child("address").getValue(String.class);
                    usergname = userSnapshot.child("guardianName").getValue(String.class);
                    usergphone = userSnapshot.child("guardianPhone").getValue(String.class);
                    userbday = userSnapshot.child("birthdate").getValue(String.class);

                    greetings.setText("Hello, " + userfname);

                } else {
                    Log.d("FirebaseData", "No user found.");
                }
            }

            public void onCancelled(DatabaseError error) {
                Log.w("FirebaseData", "Database error", error.toException());
            }
        });
    } // retrieve user data from firebase

    public void demostats () {
        Handler handler = new Handler();
        Runnable runnable =  new Runnable() {
            public void run() {

                randomBPM = ThreadLocalRandom.current().nextInt(85,111);
                randomOxygen = ThreadLocalRandom.current().nextInt(85,100);

                BPM.setText( String.valueOf(randomBPM));
                Oxygen.setText(randomOxygen+"%");


                handler.postDelayed(this, 1500); // Schedule next execution in 1 second
            }
        };
        handler.post(runnable);
    } // demo BPM and Oxygen

    public void handleLineGraph (){

        LineChart lineChart = findViewById(R.id.lineChart);

        // Data Points (Heart Rate BPM)
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 72));    // 0s
        entries.add(new Entry(30f, 75));
        entries.add(new Entry(60f, 78));   // 1 mins
        entries.add(new Entry(90f, 80));
        entries.add(new Entry(120f, 82));  // 2 mins
        entries.add(new Entry(150f, 79));
        entries.add(new Entry(180f, 85));  // 3 mins
        entries.add(new Entry(210f, 88));
        entries.add(new Entry(240f, 90));  // 4 mins
        entries.add(new Entry(270f, 87));
        entries.add(new Entry(300f, 85));  // 5 mins
        entries.add(new Entry(330f, 88));
        entries.add(new Entry(360f, 90));  // 6 mins

        // Line Graph settings
        LineDataSet lineDataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        lineDataSet.setColor(getResources().getColor(android.R.color.holo_red_light));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleColor(getResources().getColor(android.R.color.holo_green_light));
        lineDataSet.setCircleRadius(2f);
        lineDataSet.setValueTextSize(7f);

        // Add the dataset to a list (in case you want to add more datasets later)
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(lineDataSet);
        // Set LineData to chart
        LineData lineData = new LineData(dataSets);
        lineChart.setData(lineData);
        // Chart Appearance Settings
        lineChart.getDescription().setEnabled(false);
        lineChart.setDrawGridBackground(false);
        // X Axis Customization
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return (int) value + "s"; // Format as seconds
            }
        });
        // Left Y Axis Customization
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setAxisMinimum(60f); // Minimum for heart rate
        // Right Y Axis as a Border
        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(true);               // Enable axis
        rightAxis.setDrawAxisLine(true);          // Show axis line
        rightAxis.setDrawLabels(false);           // Hide text
        rightAxis.setDrawGridLines(false);        // No grid lines
        rightAxis.setAxisLineColor(Color.GRAY);   // Optional styling
        rightAxis.setAxisLineWidth(1f);           // Optional thickness

        lineChart.invalidate(); // Refresh chart to apply changes
    } // demo line graph

    public void sendText (View view){

        String text = editText.getText().toString().trim();
        String phone = editphone.getText().toString().trim();

        Toast.makeText(getApplicationContext(), "Number: " + phone + "content: " + text, Toast.LENGTH_SHORT).show();

        if (text.isEmpty() || phone.isEmpty()){
            Toast.makeText(getApplicationContext(), "Fill all Fields!!!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, text, null, null);
                Toast.makeText(getApplicationContext(), "SMS Sent Successfully!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    } // send text to guardian
}