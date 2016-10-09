package com.mhacks8.varun.emergencylocator;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private static final int uniqueID = 911911;
    private ToggleButton toggleButton;
    private static Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleButton = (ToggleButton)findViewById(R.id.toggleButton);

        if (Build.VERSION.SDK_INT >= 23) {
            if ((ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    && (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    && (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS
                }, 0);
                return;
            }
        }

        final NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent i = new Intent(getApplicationContext(), LocationService.class);
                    startService(i);

                    Intent sendHelpIntent = new Intent(getApplicationContext(), SendHelpService.class);
                    PendingIntent sendHelpPendingIntent = PendingIntent.getService(getApplicationContext(), 0, sendHelpIntent, 0);

                    Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                    PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, mainActivityIntent, 0);

                    NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext());
                    notification.setContentTitle("Emergency Alerter")
                            .setAutoCancel(false)
                            .setOngoing(true)
                            .setSmallIcon(R.drawable.alert_icon)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    notification.addAction(R.mipmap.send_help, "Send Help", sendHelpPendingIntent);
                    notification.setContentIntent(mainActivityPendingIntent);

                    Notification n = notification.build();
                    nm.notify(uniqueID, n);
                } else {
                    stopService(new Intent(getApplicationContext(), LocationService.class));
                    stopService(new Intent(getApplicationContext(), SendHelpService.class));
                    nm.cancel(uniqueID);
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        bundle.putBoolean("ToggleButtonState", toggleButton.isChecked());
    }

    @Override
    protected void onResume() {
        super.onResume();
        toggleButton.setChecked(bundle.getBoolean("ToggleButtonState"));
    }

}
