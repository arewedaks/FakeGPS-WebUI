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
    
    // Simpan lokasi terakhir
    private double currentLat = -6.175392;
    private double currentLng = 106.827153;
    private boolean shouldPushLocation = false;

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "fakegps_channel", "Fake GPS Service", android.app.NotificationManager.IMPORTANCE_LOW);
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);

            android.app.Notification notification = new android.app.Notification.Builder(this, "fakegps_channel")
                    .setContentTitle("Fake GPS Aktif")
                    .setContentText("Aplikasi berjalan di latar belakang")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .build();

            if (android.os.Build.VERSION.SDK_INT >= 29) { // Q
                startForeground(1, notification, 8); // 8 is FOREGROUND_SERVICE_TYPE_LOCATION
            } else {
                startForeground(1, notification);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.post(fetchRunnable);
        handler.post(pushRunnable);
        return START_STICKY;
    }

    private Runnable fetchRunnable = new Runnable() {
        @Override
        public void run() {
            new Thread(new NetworkTask()).start();
            handler.postDelayed(this, 3000); // Polling JSON setiap 3 detik
        }
    };

    private Runnable pushRunnable = new Runnable() {
        @Override
        public void run() {
            if (isMocking && shouldPushLocation) {
                pushLocationToProviders();
            }
            handler.postDelayed(this, 500); // Tembak lokasi setiap 500ms
        }
    };

    private class NetworkTask implements Runnable {
        @Override
        public void run() {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    Log.e("FakeGPS", "Server error: " + responseCode);
                    sendLog("Server error: HTTP " + responseCode);
                    return;
                }

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                String json = sb.toString();
                Log.d("FakeGPS", "Received JSON: " + json);
                sendLog("Berhasil tarik JSON: " + json);
                
                JSONObject obj = new JSONObject(json);
                String status = obj.optString("status", "start");

                if (status.equals("stop")) {
                    if (isMocking) {
                        try {
                            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
                            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                        } catch (Exception e) {}
                        isMocking = false;
                        shouldPushLocation = false;
                        sendLog("Mocking dihentikan (Status = stop)");
                    }
                    return; // Berhenti kirim lokasi palsu
                } else {
                    if (!isMocking) {
                        try {
                            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
                            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
                        } catch (Exception e) {}

                        try {
                            // GPS Provider
                            locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 1, 1);
                            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
                            
                            // Network Provider
                            locationManager.addTestProvider(LocationManager.NETWORK_PROVIDER, false, false, false, false, true, true, true, 1, 2);
                            locationManager.setTestProviderEnabled(LocationManager.NETWORK_PROVIDER, true);
                            
                            isMocking = true;
                            sendLog("Mocking dimulai");
                        } catch (SecurityException e) {
                            Log.e("FakeGPS", "Mock Location belum diset di opsi pengembang!");
                            sendLog("ERROR: Mock Location belum diaktifkan di Opsi Pengembang! (SecurityException)");
                            return;
                        } catch (Exception e) {
                            sendLog("ERROR Provider: " + e.getMessage());
                            return;
                        }
                    }
                }

                currentLat = obj.optDouble("lat", -6.175392);
                currentLng = obj.optDouble("lng", 106.827153);
                shouldPushLocation = true;
                
            } catch (Exception e) {
                e.printStackTrace();
                sendLog("ERROR Jaringan/JSON: " + e.getMessage());
            }
        }
    }

    private void pushLocationToProviders() {
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = SystemClock.elapsedRealtimeNanos();

            // Set GPS Location
            Location mockGps = new Location(LocationManager.GPS_PROVIDER);
            mockGps.setLatitude(currentLat);
            mockGps.setLongitude(currentLng);
            mockGps.setAltitude(10.0);
            mockGps.setTime(currentTime);
            mockGps.setAccuracy(1.0f);
            mockGps.setElapsedRealtimeNanos(elapsedTime);
            locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockGps);

            // Set Network Location
            Location mockNet = new Location(LocationManager.NETWORK_PROVIDER);
            mockNet.setLatitude(currentLat);
            mockNet.setLongitude(currentLng);
            mockNet.setAltitude(10.0);
            mockNet.setTime(currentTime);
            mockNet.setAccuracy(5.0f);
            mockNet.setElapsedRealtimeNanos(elapsedTime);
            locationManager.setTestProviderLocation(LocationManager.NETWORK_PROVIDER, mockNet);
            
            // Opsional: tidak perlu dilog tiap 500ms agar log tidak penuh
            // sendLog("Lokasi disetel: " + currentLat + ", " + currentLng);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendLog(String message) {
        Intent intent = new Intent("com.project.fakegps.LOG_EVENT");
        intent.putExtra("message", message);
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(fetchRunnable);
        handler.removeCallbacks(pushRunnable);
        try {
            locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
            locationManager.removeTestProvider(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {}
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
