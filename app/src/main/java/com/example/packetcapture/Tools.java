package com.example.packetcapture;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
public class Tools {

    // TODO - 使用 bindService 取代這個方法
    public static boolean isServiceRunning(Context context, Class<? extends Service> serviceClass){
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
