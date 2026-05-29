package com.project.fakegps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MockLocationService extends Service {
    private LocationManager locationManager;
    private Handler handler = new Handler();
    // GANTI URL DI BAWAH INI SESUAI DENGAN ALAMAT SERVER PHP ANDA
    private String apiUrl = "http://127.0.0.1/tools/fakegps/api_lokasi.php"; 

    private boolean isMocking = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(updateLocationRunnable);
        return START_STICKY;
    }

    private class UpdateLocationTask implements Runnable {
        @Override
        public void run() {
            new Thread(new NetworkTask()).start();
            handler.postDelayed(this, 3000); // Polling setiap 3 detik
        }
    }

    private class NetworkTask implements Runnable {
        @Override
        public void run() {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String json = reader.readLine();
                JSONObject obj = new JSONObject(json);
                
                String status = obj.optString("status", "start");

                if (status.equals("stop")) {
                    if (isMocking) {
                        try {
                            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
                        } catch (Exception e) {}
                        isMocking = false;
                    }
                    return; // Berhenti kirim lokasi palsu
                } else {
                    if (!isMocking) {
                        try {
                            locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0, 5);
                            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                            isMocking = true;
                        } catch (Exception e) {
                            Log.e("FakeGPS", "Mock Location belum diset di opsi pengembang!");
                        }
                    }
                }

                double lat = obj.getDouble("lat");
                double lng = obj.getDouble("lng");

                Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
                mockLocation.setLatitude(lat);
                mockLocation.setLongitude(lng);
                mockLocation.setAltitude(10.0);
                mockLocation.setTime(System.currentTimeMillis());
                mockLocation.setAccuracy(1.0f);
                mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

                locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Runnable updateLocationRunnable = new UpdateLocationTask();

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateLocationRunnable);
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
