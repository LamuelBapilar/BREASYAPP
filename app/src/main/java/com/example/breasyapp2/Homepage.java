package com.example.breasyapp2;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
//firebase
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Homepage extends AppCompatActivity {

    //UI components
    TextView Battery, BPM, Oxygen, Session, greetings, breathTextView, tempview, humview, wethview, airview;
    String useremail, userfname, userlname, userbday, useraddress, usergphone, usergname, userdose,  Weather, City, AirQuality ;
    double Temperature, HeatIndex;
    int Humidity, AQI;
    Switch BTswitch;
    //Line Graph
    int randomBPM = 0, randomOxygen = 0;
    boolean isRecordingBPM = false;
    private Handler heartRateHandler;
    private Runnable heartRateRunnable;
    //Blutooth
    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    BluetoothSocket btSocket;
    OutputStream outputStream;
    InputStream inputStream;
    final String deviceName = "ESP32_BT";
    final UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int CONNECTION_TIMEOUT = 10000; // Timeout in milliseconds (e.g., 10 seconds)
    boolean isBTconnected = false;
    boolean foundESP32 = false;
    // Onchange Listener
    observeisRecording ObserveisRecordingBPM = new observeisRecording();
    //Weather API
    private FusedLocationProviderClient fusedLocationClient;
    private final String Weather_KEY = "17561e030e960239b678acac686e1ab6"; // Replace with your OpenWeatherMap API key
    //Breathing pattern Guide
    private View breathcircle;
    private AnimatorSet breathingSet;
    private ValueAnimator colorAnimator; // Add this to your class variables

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

        ObserveisRecordingBPM.setOnChangeListener(newValue -> {
            if (newValue) {
                startHeartRateRecording(); // start BPM in line chart
                startBreathingAnimation(); // start Breathing Guide
            } else {
                heartRateHandler.removeCallbacks(heartRateRunnable); //Stop BPM in line chart
                stopBreathingAnimation(); // stop Breathing Guide
            }
        }); // observe isRecording variable for accurate execution

        // String Declarations
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE); // retrieve user email from login
        useremail = prefs.getString("useremail", null); // set retrieve email

        // TextView Declarations
        BPM = findViewById(R.id.bpm);
       // Oxygen = findViewById(R.id.Oxygen);
        Session = findViewById(R.id.Session);
        Battery = findViewById(R.id.battery);
        greetings = findViewById(R.id.greetings);
        // tempview = findViewById(R.id.tempview);
        // humview = findViewById(R.id.humview);
        // wethview = findViewById(R.id.wethview);
        // airview = findViewById(R.id.airview);

        // Switch Declarations
        BTswitch = findViewById(R.id.switch1);
        BTswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkBluetoothStatus();
                connectToESP32();
                demoBTstats();
            } else {
                Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();
                isRecordingBPM = false;

                // Prevent calling removeCallbacks on a null handler
                if (heartRateHandler != null && heartRateRunnable != null) {
                    heartRateHandler.removeCallbacks(heartRateRunnable);
                }

                // Safely close the Bluetooth socket and streams
                try {
                    if (inputStream != null) inputStream.close();
                    if (outputStream != null) outputStream.close();
                    if (btSocket != null) btSocket.close();
                } catch (IOException e) {
                    Log.e("BT", "Error closing Bluetooth", e);
                }

                isBTconnected = false;
                inputStream = null;
                outputStream = null;
                btSocket = null;
            }
        }); //BT switch

        // Location Declaration
        // fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //Breathing pattern guide Declaration
        breathcircle = findViewById(R.id.breathCircle);
        breathTextView = findViewById(R.id.breathGuide);

        //startup function
        checkBluetoothStatus();
        connectToESP32();
        demoBTstats();
        retrieveuserdata(); //retrieve user data
        handleDemoLineGraph();
        checkbtconnection();
        // fetchLocation();

    }

    //Heartbeat Line graph dependencies
    ArrayList<Entry> entries = new ArrayList<>();
    int timeCounter = 0; // Time in seconds (e.g., 0, 30, 60, ...

    public void Logout(View view){
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().remove("useremail").apply(); // remove saved user email
        prefs.edit().remove("userfname").apply();
        prefs.edit().remove("userlname").apply();
        prefs.edit().remove("userbday").apply();
        prefs.edit().remove("useraddress").apply();
        prefs.edit().remove("usergphone").apply();
        prefs.edit().remove("usergname").apply();
        prefs.edit().remove("userdose").apply();
        Intent intent = new Intent(Homepage.this, MainActivity.class);
        startActivity(intent);
    } // logout to login page

    public void Records(View view){

        if (isRecordingBPM) {
            Toast.makeText(this, "Session on progress", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Homepage.this, Records.class);
            startActivity(intent);
        }

    } // goto user's record page

    public void Profile(View view){

        if (isRecordingBPM) {
            Toast.makeText(this, "Session on progress", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(Homepage.this, Profilepage.class);
            startActivity(intent);
        }

    } // goto user's record page

    public void startHeartRateRecording() {
        heartRateHandler = new Handler();
        startBreathingAnimation();
        heartRateRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isRecordingBPM) return; // Prevents future runs if stopped

                // Simulated heart rate value
                int HeartRate = randomBPM;

                // Add new entry
                entries.add(new Entry(timeCounter, HeartRate));
                timeCounter += 1;

                // Refresh graph
                updateLineChart();

                // Continue every 1 seconds
                heartRateHandler.postDelayed(this, 1270);
            }
        };

        heartRateHandler.post(heartRateRunnable);
    } // to start BPM monitor

    public void resetSession (View view) {

        List<HeartRateRecord> sessionRecords = new ArrayList<>();
        for (Entry entry : entries) {
            int time = (int) entry.getX();
            int bpm = (int) entry.getY();
            sessionRecords.add(new HeartRateRecord(time, bpm));
        }

        // Check if sessionRecords is empty (no BPM data)
        if (sessionRecords.isEmpty()) {
            Toast.makeText(this, "No BPM data to reset", Toast.LENGTH_SHORT).show();
            return; // Exit the method if there's no data to save
        }

        if (isRecordingBPM) {
            Toast.makeText(this, "Session on progress", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, getClass());
            finish();
            startActivity(intent);
            Toast.makeText(this, "Session reset", Toast.LENGTH_SHORT).show();
        }


    }

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

        // Check if sessionRecords is empty (no BPM data)
        if (sessionRecords.isEmpty()) {
            Toast.makeText(this, "No BPM data to save", Toast.LENGTH_SHORT).show();
            return; // Exit the method if there's no data to save
        }

        if (isRecordingBPM) {
            Toast.makeText(this, "Session on progress", Toast.LENGTH_SHORT).show();
        } else {
            // Create a wrapper map to store both BPM records and weather
            Map<String, Object> sessionData = new HashMap<>();
            sessionData.put("bpmRecords", sessionRecords);

            // Include weather data
            //Map<String, Object> weatherInfo = new HashMap<>();
            //weatherInfo.put("city", City);
            // weatherInfo.put("temperature", Temperature);
            //  weatherInfo.put("feels_like", HeatIndex);
            // weatherInfo.put("humidity", Humidity);
            //  weatherInfo.put("weather_condition", Weather);
            // weatherInfo.put("air_quality", AirQuality);

            // sessionData.put("weather", weatherInfo);

            // Save to Firebase and handle success/failure
            recordsRef.child(userchild).child(sessionKey).setValue(sessionData)
                    .addOnSuccessListener(aVoid -> {
                        Intent intent = new Intent(this, getClass());
                        finish();
                        startActivity(intent);
                        Toast.makeText(this, "Session saved", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to save session", Toast.LENGTH_SHORT).show()
                    );
        }
    } // to save BPM monitor and weather data

    private void updateLineChart() {
        LineChart lineChart = findViewById(R.id.lineChart);
        LineDataSet lineDataSet = new LineDataSet(entries, "Heart Rate (BPM)");
        lineDataSet.setColor(Color.RED);

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
                    userdose = userSnapshot.child("dose").getValue(String.class);

                    SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userfname", userfname);
                    editor.putString("userlname", userlname);
                    editor.putString("userbday", userbday);
                    editor.putString("useraddress", useraddress);
                    editor.putString("usergphone", usergphone);
                    editor.putString("usergname", usergname);
                    editor.putString("userdose", userdose);
                    editor.apply();

                    greetings.setText("Hello, " + userfname );

                } else {
                    Log.d("FirebaseData", "No user found.");
                }
            }

            public void onCancelled(DatabaseError error) {
                Log.w("FirebaseData", "Database error", error.toException());
            }
        });
    } // retrieve user data from firebase

    public void demoBTstats() {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    if (inputStream != null && inputStream.available() > 0) {
                        byte[] buffer = new byte[1024];
                        int bytes = inputStream.read(buffer);
                        String message = new String(buffer, 0, bytes).trim();

                        // Expected format: "93,97,true,30"
                        String[] parts = message.split(",");

                        if (parts.length == 4) {
                            String bpmValue = parts[0];
                            String batteryValue = parts[1];
                            String runningStatus = parts[2];
                            String SessionTime = parts[3];

                            BPM.setText(bpmValue); // BPM TextView
                            Battery.setText(batteryValue + "%"); // Battery TextView
                            Session.setText(SessionTime); //Session Textview
                            randomBPM = Integer.parseInt(bpmValue);
                            isRecordingBPM = Boolean.parseBoolean(runningStatus);
                            ObserveisRecordingBPM.set(isRecordingBPM); //set observed isRecordingBPM
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                handler.postDelayed(this, 200); // Run again every 0.5s
            }
        };
        handler.post(runnable);
    } // demo BPM and Oxygen sent from ESP32

    private void checkBluetoothStatus() {
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_LONG).show();
            return;
        }

        if (!btAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, 1);
        }
    } //check if bluetooth is enabled

    private void connectToESP32() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        foundESP32 = false;

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName().equals(deviceName)) {
                foundESP32 = true;

                new Thread(() -> {
                    try {
                        btSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);
                        btAdapter.cancelDiscovery();

                        runOnUiThread(() ->
                                Toast.makeText(this, "Connecting to ESP32...", Toast.LENGTH_SHORT).show()
                        );

                        // Wait a bit to ensure ESP32 is ready
                        Thread.sleep(1500);

                        boolean connected = false;
                        int attempts = 0;

                        while (!connected && attempts < 2) {
                            try {
                                btSocket.connect();
                                connected = true;
                            } catch (IOException connectException) {
                                attempts++;
                                if (attempts >= 2) {
                                    throw connectException; // Final failure
                                }
                                Thread.sleep(1000); // Wait before retrying
                            }
                        }

                        // Connection succeeded
                        outputStream = btSocket.getOutputStream();
                        inputStream = btSocket.getInputStream();
                        isBTconnected = true;

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Connected to ESP32", Toast.LENGTH_SHORT).show();
                            BTswitch.setChecked(true);
                            sendDoseAndPhone(btSocket, userdose, usergphone);
                        });

                    } catch (IOException | InterruptedException e) {
                        isBTconnected = false;

                        try {
                            if (btSocket != null) btSocket.close();
                        } catch (IOException ex) {
                            Log.e("BT", "Socket close failed", ex);
                        }

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Failed to connect (ESP32 might be off)", Toast.LENGTH_LONG).show();
                            BTswitch.setChecked(false);
                        });

                        Log.e("BT", "Connection failed", e);
                    }
                }).start();

                break; // exit loop once device is found
            }
        }

        if (!foundESP32) {
            runOnUiThread(() ->
                    Toast.makeText(this, "ESP32 not found in paired devices", Toast.LENGTH_LONG).show()
            );
            BTswitch.setChecked(false);
        }
    }

    public void handleDemoLineGraph (){

        LineChart lineChart = findViewById(R.id.lineChart);
        lineChart.clear();
        // Data Points (Heart Rate BPM)
        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0f, 0));    // 0s
        entries.add(new Entry(30f, 0));
        entries.add(new Entry(60f, 0));   // 1 mins
        entries.add(new Entry(90f, 0));
        entries.add(new Entry(120f, 0));  // 2 mins
        entries.add(new Entry(150f, 0));
        entries.add(new Entry(180f, 0));  // 3 mins
        entries.add(new Entry(210f, 0));
        entries.add(new Entry(240f, 0));  // 4 mins
        entries.add(new Entry(270f, 0));
        entries.add(new Entry(300f, 0));  // 5 mins
        entries.add(new Entry(330f, 0));
        entries.add(new Entry(360f, 0));  // 6 mins

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

        entries.clear();                      // Clear all Entry data points
        lineChart.invalidate();              // Redraw the chart
    } // demo line graph

    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    public void sendmyText(View view) {
        String text = "BREASY Automated Alarm: "+ userfname + " " + userlname + " session is over, high BPM warning!!! Requesting Assitance at " + useraddress;
        String phone = usergphone;

        if (text.isEmpty() || phone.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Fill all fields!!!", Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getApplicationContext(), "Sending...", Toast.LENGTH_SHORT).show();

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
                int defaultSmsSubscriptionId = SubscriptionManager.getDefaultSmsSubscriptionId();
                SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(defaultSmsSubscriptionId);
                smsManager.sendTextMessage(phone, null, text, null, null);
            } else {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phone, null, text, null, null);
            }

            Toast.makeText(getApplicationContext(), "SMS Sent Successfully!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    } // send text to guardian

    private void startBreathingAnimation() {
        int colorStart = Color.parseColor("#6DD19C");
        int colorEnd = Color.parseColor("#f6cb85");

        // INHALE: scale up + color transition
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(breathcircle, "scaleX", 1f, 1.4f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(breathcircle, "scaleY", 1f, 1.4f);
        scaleUpX.setDuration(3000);
        scaleUpY.setDuration(3000);

        ValueAnimator inhaleColor = ValueAnimator.ofArgb(colorStart, colorEnd);
        inhaleColor.setDuration(3000);
        inhaleColor.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            breathcircle.setBackgroundTintList(ColorStateList.valueOf(color));
        });

        AnimatorSet inhaleSet = new AnimatorSet();
        inhaleSet.playTogether(scaleUpX, scaleUpY, inhaleColor);
        inhaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                breathTextView.setText("Inhale");
            }
        });

        // EXHALE: scale down + color reverse
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(breathcircle, "scaleX", 1.4f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(breathcircle, "scaleY", 1.4f, 1f);
        scaleDownX.setDuration(4000);
        scaleDownY.setDuration(4000);

        ValueAnimator exhaleColor = ValueAnimator.ofArgb(colorEnd, colorStart);
        exhaleColor.setDuration(4000);
        exhaleColor.addUpdateListener(animation -> {
            int color = (int) animation.getAnimatedValue();
            breathcircle.setBackgroundTintList(ColorStateList.valueOf(color));
        });

        AnimatorSet exhaleSet = new AnimatorSet();
        exhaleSet.playTogether(scaleDownX, scaleDownY, exhaleColor);
        exhaleSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                breathTextView.setText("Exhale");
            }
        });

        // LOOP: inhale → exhale
        breathingSet = new AnimatorSet();
        breathingSet.playSequentially(inhaleSet, exhaleSet);
        breathingSet.setStartDelay(500);

        breathingSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (breathingSet != null) {
                    breathingSet.start(); // Loop
                }
            }
        });

        breathingSet.start();
    }// start breathing guide circle

    private void stopBreathingAnimation() {
        if (breathingSet != null) {
            breathingSet.removeAllListeners();
            if (breathingSet.isRunning()) {
                breathingSet.cancel();
            }
            breathingSet = null;
        }

        if (colorAnimator != null) {
            colorAnimator.cancel();
            colorAnimator = null;
        }

        // Reset breathcircle to normal scale
        ObjectAnimator resetX = ObjectAnimator.ofFloat(breathcircle, "scaleX", breathcircle.getScaleX(), 1f);
        ObjectAnimator resetY = ObjectAnimator.ofFloat(breathcircle, "scaleY", breathcircle.getScaleY(), 1f);
        resetX.setDuration(500);
        resetY.setDuration(500);

        AnimatorSet resetSet = new AnimatorSet();
        resetSet.playTogether(resetX, resetY);
        resetSet.start();

        // Reset text and color
        breathTextView.setText("Idle");
        breathcircle.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#6DD19C")));
    } // stop breathing guide circle

    public void checkbtconnection() {
        final Handler handler = new Handler();
        Runnable pingRunnable = new Runnable() {
            @Override
            public void run() {
                // Only attempt to ping if connected
                if (isBTconnected) {
                    try {
                        if (btSocket != null && btSocket.isConnected()) {
                            // Send a ping message to the ESP32
                            outputStream.write("ping".getBytes());
                        } else {
                            // If socket is no longer connected
                            isBTconnected = false;
                            isRecordingBPM = false;

                        }
                    } catch (IOException e) {
                        // Handle write failure and reconnect
                        Log.e("BT", "Ping failed", e);
                        isBTconnected = false;
                        isRecordingBPM = false;

                    }
                } else {
                    // If not connected
                    isBTconnected = false;
                    isRecordingBPM = false;

                }

                handler.postDelayed(this, 2000); // Schedule next ping in 2 seconds
            }
        };
        handler.post(pingRunnable); // Start the ping loop
    }   // Check Bluetooth connection every 5 seconds (Buggy)

    //no longer in use functions

    private void fetchLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        getWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Failed to get location", Toast.LENGTH_SHORT).show();
                    }
                });
    } //fetch User location

    private void getWeather(double lat, double lon) {
        OkHttpClient client = new OkHttpClient();

        String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + Weather_KEY;

        String airUrl = "https://api.openweathermap.org/data/2.5/air_pollution?lat=" + lat +
                "&lon=" + lon + "&appid=" + Weather_KEY;

        Request weatherRequest = new Request.Builder().url(weatherUrl).build();
        client.newCall(weatherRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(Homepage.this, "❌ Weather fetch failed.", Toast.LENGTH_SHORT).show());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String json = response.body().string();
                        JSONObject obj = new JSONObject(json);

                        City = obj.getString("name");
                        JSONObject main = obj.getJSONObject("main");
                        Temperature = main.getDouble("temp");
                        HeatIndex = main.getDouble("feels_like");
                        Humidity = main.getInt("humidity");
                        Weather = obj.getJSONArray("weather")
                                .getJSONObject(0).getString("main");

                        // Fetch AQI data now
                        Request airRequest = new Request.Builder().url(airUrl).build();
                        client.newCall(airRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> Toast.makeText(Homepage.this, "🌡 Weather OK\n🌫 AQI fetch failed.", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(Call call, Response airResponse) throws IOException {
                                if (airResponse.isSuccessful()) {
                                    try {
                                        String airJson = airResponse.body().string();
                                        JSONObject airObj = new JSONObject(airJson);
                                        AQI = airObj.getJSONArray("list")
                                                .getJSONObject(0)
                                                .getJSONObject("main")
                                                .getInt("aqi");

                                        String aqiStatus;
                                        switch (AQI) {
                                            case 1:
                                                AirQuality = "Good ✅";
                                                break;
                                            case 2:
                                                AirQuality = "Fair 🙂";
                                                break;
                                            case 3:
                                                AirQuality = "Moderate 😐";
                                                break;
                                            case 4:
                                                AirQuality = "Poor 😷";
                                                break;
                                            case 5:
                                                AirQuality = "Very Poor 🚨";
                                                break;
                                            default:
                                                AirQuality = "Unknown";
                                        }

                                        String display = "📍 Location: " + City + "\n" +
                                                "🌡 Temp: " + Temperature + "°C\n" +
                                                "🥵 Feels Like: " + HeatIndex + "°C\n" +
                                                "💧 Humidity: " + Humidity + "%\n" +
                                                "🌤 Weather: " + Weather + "\n" +
                                                "🌫 Air Quality: " + AirQuality;

                                        // Run UI updates on the main thread
                                        runOnUiThread(() -> {
                                            tempview.setText(Temperature + "°C");
                                            humview.setText(Humidity + "%");
                                            wethview.setText(Weather);
                                            airview.setText(AirQuality);
                                        });

                                    } catch (Exception e) {
                                        runOnUiThread(() -> Toast.makeText(Homepage.this, "\n⚠️ AQI parse error.", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            }
                        });

                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(Homepage.this, "⚠️ Weather parse error.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(Homepage.this, "🚫 Weather API error.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    } // gather weather data from location given

    private void connectToESP32old() {
        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        Thread connectionThread = new Thread(() -> {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(deviceName)) {
                    foundESP32 = true;
                    try {
                        btSocket = device.createRfcommSocketToServiceRecord(SERIAL_UUID);

                        // Start the connection attempt in a separate thread
                        Thread connectThread = new Thread(() -> {
                            try {
                                btSocket.connect();
                                outputStream = btSocket.getOutputStream();
                                inputStream = btSocket.getInputStream();

                                // Successfully connected
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Connection Success", Toast.LENGTH_SHORT).show();
                                    isBTconnected = true;
                                    BTswitch.setChecked(true);
                                });
                            } catch (IOException e) {
                                // Connection failed
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
                                    isBTconnected = false;
                                    BTswitch.setChecked(false);
                                });
                                Log.e("BT", "Connection failed", e);
                            }
                        });

                        // Start the connection thread
                        connectThread.start();

                        // Set a timeout for the connection attempt
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            if (!isBTconnected) {
                                try {
                                    btSocket.close(); // Close socket if connection is not established
                                } catch (IOException e) {
                                    Log.e("BT", "Error closing socket", e);
                                }
                                runOnUiThread(() -> {
                                    Toast.makeText(getApplicationContext(), "Connection Timeout", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }, CONNECTION_TIMEOUT); // Timeout after 10 seconds

                    } catch (IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(), "Failed to connect", Toast.LENGTH_SHORT).show();
                        });
                        Log.e("BT", "Connection setup failed", e);
                        isBTconnected = false;
                        BTswitch.setChecked(false);
                    }
                    break;
                }
            }

            // If ESP32 not found
            if (!foundESP32) {
                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "ESP32 not found. Please pair it first.", Toast.LENGTH_LONG).show();
                    BTswitch.setChecked(false);
                });
            }
        });

        // Start the pairing search on a new thread to avoid blocking the UI
        connectionThread.start();
    } //

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

    public void continueHeartRateRecording(View view) {

        if (!isRecordingBPM){
            startHeartRateRecording();
        } else {
            Toast.makeText(this, "Session already started", Toast.LENGTH_SHORT).show();
        }

    }

    public void stopHeartRateRecording(View view){
        isRecordingBPM = false;
        heartRateHandler.removeCallbacks(heartRateRunnable);
        stopBreathingAnimation();
    }; // to stop BPM monitor

    public void saveSessionold(View view) {
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
            handleDemoLineGraph();
            Toast.makeText(this, "Session saved to Firebase", Toast.LENGTH_SHORT).show();
        }

    } // to save BPM monitor

    public void sendDoseAndPhone(BluetoothSocket bluetoothSocket, String dose, String phone) {
        if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
            try {
                String message = "DOSE:" + dose + ",PHONE:" + phone + "\n";
                OutputStream outputStream = bluetoothSocket.getOutputStream();
                outputStream.write(message.getBytes());
                outputStream.flush();
                Log.d("Bluetooth", "Sent: " + message);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Bluetooth", "Failed to send data: " + e.getMessage());
            }
        } else {
            Log.w("Bluetooth", "Socket not connected!");
        }
    }

}