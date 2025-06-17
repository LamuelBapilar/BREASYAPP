package com.example.breasyapp2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class experiment extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_experiment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    public void SessionAlert(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Interval")
                .setMessage("Do you want to set your next session?")
                .setPositiveButton("Yes", (dialog, which) -> nextSessionAlert())
                .setNegativeButton("No", null)  // no need to manually dismiss
                .show();
    }

    public void nextSessionAlert() {

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 40, 50, 10);
        TextView label = new TextView(this);
        label.setText("Set next Session here:");
        label.setTextSize(16);
        label.setPadding(0, 0, 0, 5); // konting spacing below
        final EditText input = new EditText(this);
        input.setHint("Minutes");
        input.setEms(5);

        container.addView(label);
        container.addView(input);

        // Build dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Next Session")
                .setView(container)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button okButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            okButton.setOnClickListener(view -> {
                String inputValue = input.getText().toString().trim();

                if (inputValue.isEmpty()) {
                    input.setError("Required");
                    return;
                }

                int seconds;
                try {
                    seconds = Integer.parseInt(inputValue);
                } catch (NumberFormatException e) {
                    input.setError("Enter a valid number");
                    return;
                }

                dialog.dismiss();
                setCountdownNotification(seconds); // call your timer here
            });
        });

        dialog.show();
    }


    public void setCountdownNotification(int minutes) {
        Intent serviceIntent = new Intent(this, CountdownService.class);
        serviceIntent.putExtra(CountdownService.EXTRA_DURATION, minutes * 60 * 1000L);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }


}
