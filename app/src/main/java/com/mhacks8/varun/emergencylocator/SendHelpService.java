package com.mhacks8.varun.emergencylocator;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SendHelpService extends Service {

    public SendHelpService() {
    }

    private Thread t;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences sharedPref = getSharedPreferences("userInfo", MODE_PRIVATE);
        final SmsManager smsManager = SmsManager.getDefault();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (LocationService.getLat() != -1 && LocationService.getLng() != -1) {
                        if (sharedPref.getBoolean("policeChecked", false)) {
                            smsManager.sendTextMessage("+18138429368", null, "Help Me @ (" + LocationService.getLat() + ", " + LocationService.getLng() + ")", null, null);
                        }
                        if (sharedPref.getBoolean("personalChecked", false)) {
                            smsManager.sendTextMessage(sharedPref.getString("personalNumber", "+12095539079"), null, "Help Me @ (" + LocationService.getLat() + ", " + LocationService.getLng() + ")", null, null);
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t = new Thread(r);
        t.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        t.interrupt();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
