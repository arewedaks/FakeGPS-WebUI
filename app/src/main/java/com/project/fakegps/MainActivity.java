package com.project.fakegps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity implements android.view.View.OnClickListener {

    private android.widget.TextView tvLog;
    private android.content.BroadcastReceiver logReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent.hasExtra("message") && tvLog != null) {
                String msg = intent.getStringExtra("message");
                String current = tvLog.getText().toString();
                // Keep only last 2000 chars to avoid memory issues
                if (current.length() > 2000) {
                    current = current.substring(0, 2000);
                }
                tvLog.setText(msg + "\n" + current);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLog = findViewById(R.id.tvLog);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, 100);
            
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{
                    "android.permission.POST_NOTIFICATIONS"
                }, 101);
            }
        }

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        
        // Register receiver
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            registerReceiver(logReceiver, new android.content.IntentFilter("com.project.fakegps.LOG_EVENT"), RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(logReceiver, new android.content.IntentFilter("com.project.fakegps.LOG_EVENT"));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(logReceiver);
        } catch (Exception e) {}
    }

    @Override
    public void onClick(android.view.View v) {
        if (v.getId() == R.id.btnStart) {
            tvLog.setText("Mencoba memulai service...\n" + tvLog.getText());
            Intent serviceIntent = new Intent(MainActivity.this, MockLocationService.class);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            Toast.makeText(MainActivity.this, "Fake GPS Dimulai", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btnStop) {
            tvLog.setText("Service dihentikan.\n" + tvLog.getText());
            stopService(new Intent(MainActivity.this, MockLocationService.class));
            Toast.makeText(MainActivity.this, "Fake GPS Dihentikan", Toast.LENGTH_SHORT).show();
        }
    }
}
