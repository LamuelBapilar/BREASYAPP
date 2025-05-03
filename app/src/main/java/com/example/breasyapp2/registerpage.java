package com.example.breasyapp2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.HashMap;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
//firebase imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class registerpage extends AppCompatActivity {

    // Global Parameters
    public EditText fnameInput, lnameInput, emailInput, passwordInput, bdateInput, gnameInput, gphoneInput, addressInput, doseInput;
    public String   fname,  lname,  email, password,  bdate,  gname,  gphone,  address, dose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registerpage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // textInput declarations
        fnameInput = findViewById(R.id.Fname);
        lnameInput = findViewById(R.id.Lname);
        emailInput = findViewById(R.id.Email);
        passwordInput = findViewById(R.id.Password);
        bdateInput = findViewById(R.id.Bdate);
        gnameInput = findViewById(R.id.Gname);
        gphoneInput = findViewById(R.id.GPhone);
        addressInput = findViewById(R.id.Address);
        doseInput = findViewById(R.id.Dose);

    }


    // function to access sign up page
    public void gotoLoginpage(View view){
        Intent intent = new Intent(registerpage.this, MainActivity.class);
        startActivity(intent);
    }

    // function for Signing Up an Account
    public void signUpAccount(View view) {

        // Call Input data
        fname = fnameInput.getText().toString().trim();
        lname = lnameInput.getText().toString().trim();
        email = emailInput.getText().toString().trim();
        password = passwordInput.getText().toString().trim();
        bdate = bdateInput.getText().toString().trim();
        gname = gnameInput.getText().toString().trim();
        gphone = gphoneInput.getText().toString().trim();
        address = addressInput.getText().toString().trim();
        dose = doseInput.getText().toString().trim();

        // Registration Process
        if (!fname.isEmpty() && !lname.isEmpty() && !email.isEmpty() && !password.isEmpty() &&
                !bdate.isEmpty() && !gname.isEmpty() && !gphone.isEmpty() && !address.isEmpty() && !dose.isEmpty()) {

            // Initialize Firebase
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

            // Use email as user ID and replace problematic characters
            String userId = email.replace(".", "_")
                    .replace("#", "_")
                    .replace("$", "_")
                    .replace("[", "_")
                    .replace("]", "_");

            // Check if email already exists
            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Email already exists
                        Toast.makeText(getApplicationContext(), "Email already registered!", Toast.LENGTH_SHORT).show();
                    } else {
                        // Compile data using hashmap
                        HashMap<String, String> userData = new HashMap<>();
                        userData.put("firstName", fname);
                        userData.put("lastName", lname);
                        userData.put("email", email);
                        userData.put("password", password);
                        userData.put("birthdate", bdate);
                        userData.put("guardianName", gname);
                        userData.put("guardianPhone", gphone);
                        userData.put("address", address);
                        userData.put("dose", dose);

                        // Save data in Firebase
                        databaseReference.child(userId).setValue(userData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getApplicationContext(), "Sign-up Successful!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(registerpage.this, MainActivity.class);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getApplicationContext(), "Failed to Save Data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else {
            Toast.makeText(this, "Please Complete All Fields!", Toast.LENGTH_SHORT).show();
        }
    }


}