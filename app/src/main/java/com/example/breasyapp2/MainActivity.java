package com.example.breasyapp2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
// firebase
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;



public class MainActivity extends AppCompatActivity {

    // Global Parameters
    public EditText emailInput, passwordInput;
    public String   email, password;
    public Button signinbutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //checking permissions
        checkPermissions();
        // TextInput Declarations
        emailInput = findViewById(R.id.inputEmail);
        passwordInput = findViewById(R.id.inputPass);
        // Buttons Declarations
        signinbutton = findViewById(R.id.button2);
    }

    // function to check permissions (if permission not complete then login button deactivates)
    public void checkPermissions () {
        
    //request for the permissions    
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.INTERNET,
                Manifest.permission.FOREGROUND_SERVICE
        };
        ActivityCompat.requestPermissions(this, permissions, 1);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String
            [] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Check if all permissions are granted
        if (requestCode == 1 && grantResults.length > 0) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            
        // enable/disable signin button
            if (allPermissionsGranted) {
                signinbutton.setEnabled(true);
            } else {
                signinbutton.setEnabled(false);
                Toast.makeText(getApplicationContext(), "Approved all Permissions to continue", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // function to access sign up page
    public void gotoRegisterpage (View view){
        Intent intent = new Intent(MainActivity.this, registerpage.class);
        startActivity(intent);
    }

    // function to access sign up page
    public void gotoExperimental (View view){
        Intent intent = new Intent(MainActivity.this, experiment.class);
        startActivity(intent);
    }

    // Function to Sign In
    public void signInAccount (View view) {

        // Call Input data
        email = emailInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();

        // Initialize Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        if (!email.isEmpty() && !password.isEmpty()) {
            databaseReference.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                    String storedPassword = userSnapshot.child("password").getValue(String.class);

                                    if (storedPassword != null && storedPassword.equals(password)) {

                                        // Save to SharedPreferences to accessible all over the app
                                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                        prefs.edit().putString("useremail", email).apply();
                                          doseAlert();

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Incorrect Password!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Email Not Found!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            Toast.makeText(getApplicationContext(), "Please Complete All Fields!", Toast.LENGTH_SHORT).show();
        }
    }

    public void doseAlert () {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to change your dose?")
                .setTitle("Confirm")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        newdoseAlert();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(getApplicationContext(), "Sign-In Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, Homepage.class);
                        // Save to SharedPreferences to accessible all over the app
                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                        prefs.edit().putString("useremail", email).apply();

                        startActivity(intent);
                    }
                })
                .show();
    }


    public void newdoseAlert() {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String email = prefs.getString("useremail", "");

        if (email.isEmpty()) {
            Toast.makeText(MainActivity.this, "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String firebaseKey = email.replace(".", "_");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(firebaseKey)
                .child("dose");

        final EditText input = new EditText(this);
        input.setHint("Enter new dose");
        input.setEms(5);

        TextView label = new TextView(this);
        label.setText("Set your new dose:");
        label.setTextSize(16);
        label.setPadding(0, 0, 0, 5);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 40, 50, 10);
        container.addView(label);
        container.addView(input);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentDose = snapshot.getValue(String.class);
                    if (currentDose != null) {
                        input.setText(currentDose);
                    }
                }

                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Enter Dose")
                        .setView(container)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Cancel", null)
                        .create();

                dialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(view -> {
                        String dose = input.getText().toString().trim();

                        if (dose.isEmpty()) {
                            input.setError("Dose cannot be empty");
                            return;
                        }

                        userRef.setValue(dose)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(MainActivity.this, "Dose updated to " + dose, Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(MainActivity.this, Homepage.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Failed to update dose: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                        dialog.dismiss();
                    });
                });

                dialog.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error loading dose: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




}
