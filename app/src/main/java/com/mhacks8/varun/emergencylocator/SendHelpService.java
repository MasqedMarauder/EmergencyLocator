package com.mhacks8.varun.emergencylocator;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.SmsManager;

public class SendHelpService extends Service {

    public SendHelpService() {
    }

    private Thread t;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SmsManager smsManager = SmsManager.getDefault();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (LocationService.getLat() != -1 && LocationService.getLng() != -1) {
                        smsManager.sendTextMessage("+18138429368", null, "Help Me @ (" + LocationService.getLat() + ", " + LocationService.getLng() + ")", null, null);
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
