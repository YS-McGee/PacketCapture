package com.example.packetcapture;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "TestChannel")
                .setSmallIcon(android.R.drawable.star_on)
                .setContentTitle("Test Notification")
                .setContentText("Test Text 123")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);


        FloatingActionButton fab = findViewById(R.id.fab);

        NotificationManager manager = getSystemService(NotificationManager.class);
        fab.setOnClickListener( view -> manager.notify(4, builder.build()
        ));
    }

    private void createNotificationChannel(){
        // Only on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TestChannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}