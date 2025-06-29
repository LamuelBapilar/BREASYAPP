package com.example.breasyapp2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class CountdownService extends Service {
    public static final String EXTRA_DURATION = "duration"; // in milliseconds
    private NotificationManager notificationManager;
    private CountDownTimer countDownTimer;
    private final int NOTIF_ID = 1001;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        long duration = intent.getLongExtra(EXTRA_DURATION, 0);

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            createNotificationChannel();
        }

        // Cancel old countdown if running
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        // Initial notification
        startForeground(NOTIF_ID, buildNotification("Countdown started..."));

        // Start new countdown
        countDownTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
                String timeLeft = formatTime(millisUntilFinished);
                Notification notification = buildNotification("Next session in: " + timeLeft);
                notificationManager.notify(NOTIF_ID, notification);
            }

            public void onFinish() {
                // Send broadcast when countdown is done
                Intent broadcastIntent = new Intent(getApplicationContext(), MyNotificationReceiver.class);
                getApplicationContext().sendBroadcast(broadcastIntent);

                // Remove the ongoing notification
                notificationManager.cancel(NOTIF_ID);

                stopSelf();
            }
        }.start();

        return START_NOT_STICKY;
    }

    private Notification buildNotification(String contentText) {
        Intent intent = new Intent(this, experiment.class); // Change to your target activity
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(this, "countdown_channel")
                .setContentTitle("Breasy Reminder")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "countdown_channel",
                    "Countdown Timer",
                    NotificationManager.IMPORTANCE_LOW
            );
            notificationManager.createNotificationChannel(channel);
        }
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        int hours = (int) (millis / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (notificationManager != null) {
            notificationManager.cancel(NOTIF_ID);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
