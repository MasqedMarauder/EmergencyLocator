package com.mhacks8.varun.emergencylocator;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private static final int uniqueID = 911911;
    private ToggleButton toggleButton;
    private static Bundle bundle = new Bundle();
    private CheckBox policeCheckBox;
    private CheckBox personalCheckBox;
    private EditText personalPhoneEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final SharedPreferences sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        toggleButton = (ToggleButton)findViewById(R.id.toggleButton);
        policeCheckBox = (CheckBox)findViewById(R.id.policeCheckBox);
        personalCheckBox = (CheckBox)findViewById(R.id.personalCheckBox);
        personalPhoneEditText = (EditText)findViewById(R.id.personalPhoneEditText);

        if (Build.VERSION.SDK_INT >= 23) {if (checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                || checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                || checkPermission(Manifest.permission.SEND_SMS)
                || checkPermission(Manifest.permission.CHANGE_WIFI_STATE))
            {
                requestPermissions(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.CHANGE_WIFI_STATE
                }, 0);
                return;
            }
        }

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            // Build the alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Location Services Not Active");
            builder.setMessage("Please enable Location Services and GPS");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Show location settings when the user acknowledges the alert dialog
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            Dialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }

        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(true);

        policeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("policeChecked", isChecked);
                editor.apply();
            }
        });

        personalCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editor.putBoolean("personalChecked", isChecked);
                editor.apply();
            }
        });

        personalPhoneEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                editor.putString("personalNumber", "+1" + v.toString());
                editor.apply();
                return true;
            }
        });

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

    private boolean checkPermission(String permission) {
        return ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED;
    }

}
