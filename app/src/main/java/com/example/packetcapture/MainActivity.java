package com.example.packetcapture;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.InetAddresses;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.Optional;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final String TAG = getClass().getSimpleName();

    private FloatingActionButton fab;
    private TextView deviceNameView;

    private Optional<LinkProperties> wifiLink;

    private final int REQUEST_ON_CLICK = 1;

    // TODO - 如果監聽時 Wifi 中斷，應該自動清理 Service
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        deviceNameView = findViewById(R.id.deviceNameView);

        // 建立 Notification Channel
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
            String channelId = getString(R.string.channel_id);
            String channelName = getString(R.string.channel_name);
            String channelDescription = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 尋找 Wifi 連線資訊
        ConnectivityManager manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        wifiLink = Arrays.stream(manager.getAllNetworks())
                .filter( n -> manager.getNetworkCapabilities(n).hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                .findFirst()
                .map( n -> manager.getLinkProperties(n));

        // 更新 UI
        updateUI();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Snackbar.make(findViewById(android.R.id.content), "Permission granted.", Snackbar.LENGTH_SHORT);
    }

    @Override
    public void onClick(View view) {
        if (Tools.isServiceRunning(this, VowifiService.class)){
            stopVowifiService();
        }else{
            startVowifiService();
        }

        runOnUiThread(this::updateUI);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Tools.isServiceRunning(MainActivity.this, VowifiService.class)) {
            stopVowifiService();
        }
    }

    private void startVowifiService() {
        if ( ! wifiLink.isPresent()){
            Snackbar.make(findViewById(android.R.id.content), "Wifi is off.", Snackbar.LENGTH_LONG)
                    .setAction("Open", null)
                    .show();
            return;
        }

        LinkProperties link = wifiLink.get();

        Optional<String> ipaddress = link.getLinkAddresses().stream()
                .map( l -> l.getAddress() )
                .filter( ip -> (!ip.isLoopbackAddress()))
                .map( ip -> ip.getHostAddress())
                .peek( s -> Log.d(TAG, "Host IP :"+s))
                .filter( s-> s.indexOf(":") < 0)
                .findFirst();
        if( ! ipaddress.isPresent() ){
            Snackbar.make(findViewById(android.R.id.content), "Wifi is not ready.", Snackbar.LENGTH_LONG).show();
            return;
        }

        // TODO - 取得 ePDG IP

        Intent intent = new Intent(MainActivity.this, VowifiService.class)
                .setAction("START")
                .putExtra("Interface", link.getInterfaceName())
                .putExtra("Host", ipaddress.get());

        startService(intent);
    }

    private void stopVowifiService(){
        Intent intent = new Intent(MainActivity.this, VowifiService.class)
                .setAction("STOP");

        stopService(intent);
    }


    private void updateUI(){
        // fab
        if (Tools.isServiceRunning(MainActivity.this, VowifiService.class)){
            fab.setImageResource(android.R.drawable.ic_media_pause);
        }else{
            fab.setImageResource(android.R.drawable.ic_media_play);
        }

        // deviceNameView
        String interfaceName = wifiLink.isPresent() ? wifiLink.get().getInterfaceName() : "None";
        deviceNameView.setText(interfaceName);
    }

}