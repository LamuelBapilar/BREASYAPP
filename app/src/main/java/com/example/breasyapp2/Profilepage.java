package com.example.breasyapp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.PrivateKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Profilepage extends AppCompatActivity {

    private String useremail, userfname, userlname, userbday, useraddress, usergphone, usergname, userdose;
    private EditText OldPass, NewPass, RenewPass, NewDose;
    private Button passbutton, dosebutton;
    private TextView Allsessions, Lastsession, username, usergmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profilepage);
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

        //Textview Declarations
        Allsessions = findViewById(R.id.AllSession);
        Lastsession = findViewById(R.id.LastSession);
        username = findViewById(R.id.username);
        usergmail = findViewById(R.id.useremail);
        username.setText(userfname + " " + userlname);
        usergmail.setText(useremail);

        // Edittext Declarations
        OldPass = findViewById(R.id.oldpass);
        NewPass = findViewById(R.id.newpass);
        RenewPass = findViewById(R.id.renewpass);
        NewDose = findViewById(R.id.newdose);
        NewDose.setText(userdose); //put existing dose

        // Button Declarations
        passbutton = findViewById(R.id.passbutton);
        dosebutton = findViewById(R.id.dosebutton);

        //Startup Functions
        SessionStatus(); // check sessions

    }

    public void Records(View view) {

        Intent intent = new Intent(this, Records.class);
        startActivity(intent);

    } // goto user's record page

    public void Home(View view) {

        Intent intent = new Intent(this, Homepage.class);
        startActivity(intent);

    } // goto user's Home page

    public void SessionStatus() {
        DatabaseReference userSessionsRef = FirebaseDatabase.getInstance()
                .getReference("Records")
                .child(userfname + " " + userlname); // Use UID if possible

        userSessionsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                int totalSessions = (int) snapshot.getChildrenCount();
                String latestKey = null;
                long latestTime = 0;

                for (DataSnapshot sessionSnap : snapshot.getChildren()) {
                    String key = sessionSnap.getKey();
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date sessionDate = sdf.parse(key);
                        if (sessionDate != null) {
                            long time = sessionDate.getTime();
                            if (time > latestTime) {
                                latestTime = time;
                                latestKey = key;
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                long currentTime = System.currentTimeMillis();
                long daysSinceLastSession = latestTime > 0 ?
                        TimeUnit.MILLISECONDS.toDays(currentTime - latestTime) : -1;

                // Set to TextViews
                Allsessions.setText(String.valueOf(totalSessions));
                if (daysSinceLastSession >= 0) {
                    Lastsession.setText(daysSinceLastSession + " D Ago");
                } else {
                    Lastsession.setText("N/A");
                }

            } else {
                Allsessions.setText("N/A");
                Lastsession.setText("N/A");
                Log.e("SessionStatus", "Failed to retrieve data: " + task.getException());
            }
        });
    } // check all session and latest session

    public void ChangePass(View view) {
        String oldPassInput = OldPass.getText().toString().trim();
        String newPassInput = NewPass.getText().toString().trim();
        String reNewPassInput = RenewPass.getText().toString().trim();
        String userkey = useremail.replace(".", "_");

        if (oldPassInput.isEmpty() || newPassInput.isEmpty() || reNewPassInput.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassInput.equals(reNewPassInput)) {
            Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Reference to this user's data
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userkey); // replace with actual userId or email key

        userRef.child("password").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String currentPassword = task.getResult().getValue(String.class);

                if (currentPassword != null && currentPassword.equals(oldPassInput)) {
                    // Update password
                    userRef.child("password").setValue(newPassInput)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Password updated successfully, Log off", Toast.LENGTH_SHORT).show();
                                Logout();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to access user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void ChangeDose(View view) {
        String dose = NewDose.getText().toString().trim();
        String userKey = useremail.replace(".", "_"); // convert email to Firebase-safe key

        if (!dose.isEmpty()) {
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userKey)
                    .child("dose")
                    .setValue(dose)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Dose updated, logging Off", Toast.LENGTH_SHORT).show();
                        Logout();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Please enter a dose", Toast.LENGTH_SHORT).show();
        }
    }

    public void Logout(){
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        prefs.edit().remove("useremail").apply(); // remove saved user email
        prefs.edit().remove("userfname").apply();
        prefs.edit().remove("userlname").apply();
        prefs.edit().remove("userbday").apply();
        prefs.edit().remove("useraddress").apply();
        prefs.edit().remove("usergphone").apply();
        prefs.edit().remove("usergname").apply();
        prefs.edit().remove("userdose").apply();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    } // logout to login page





}