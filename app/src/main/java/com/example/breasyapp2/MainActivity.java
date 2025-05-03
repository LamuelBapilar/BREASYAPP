package com.example.breasyapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
                Manifest.permission.ACCESS_FINE_LOCATION
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
        Intent intent = new Intent(MainActivity.this, Profilepage .class);
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

                                        Toast.makeText(getApplicationContext(), "Sign-In Successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, Homepage.class);
                                        // Save to SharedPreferences to accessible all over the app
                                        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                        prefs.edit().putString("useremail", email).apply();

                                        startActivity(intent);

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

}
