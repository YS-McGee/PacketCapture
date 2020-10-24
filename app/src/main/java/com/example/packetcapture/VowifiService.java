package com.example.packetcapture;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.packetcapture.bin.PacketSniffer;

public class VowifiService extends Service {

    private final String TAG = getClass().getSimpleName();

    private Thread innerThread;
    private PacketSniffer sniffer;

    public VowifiService() { }

    @Override
    public void onCreate() {
        super.onCreate();

        //建立 Foreground Service
        String channelId = getString(R.string.channel_id);
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.alert_light_frame)
                .setContentTitle(getString(R.string.vowifiservice_display_name))
                .setContentText(getString(R.string.vowifiservice_description))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build();

        startForeground( getResources().getInteger(R.integer.vowifiservice_notification_id), notification);

        Log.d(TAG, "Created.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (innerThread != null){
            // 已執行過
            Log.d(TAG, "A service has been already running.");

            return START_NOT_STICKY;
        }
        Log.d(TAG, "Start.");

        sniffer = new PacketSniffer();
        innerThread = new Thread(sniffer);

        // TODO - 調高優先權

        innerThread.start();

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "Stopping service.");
        sniffer.stop();

        try {
            innerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Destroyed.");
    }
}
