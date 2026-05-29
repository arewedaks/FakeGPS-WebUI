package com.project.fakegps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity implements android.view.View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = findViewById(R.id.btnStart);
        Button btnStop = findViewById(R.id.btnStop);

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
    }

    @Override
    public void onClick(android.view.View v) {
        if (v.getId() == R.id.btnStart) {
            startService(new Intent(MainActivity.this, MockLocationService.class));
            Toast.makeText(MainActivity.this, "Fake GPS Dimulai", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.btnStop) {
            stopService(new Intent(MainActivity.this, MockLocationService.class));
            Toast.makeText(MainActivity.this, "Fake GPS Dihentikan", Toast.LENGTH_SHORT).show();
        }
    }
}
