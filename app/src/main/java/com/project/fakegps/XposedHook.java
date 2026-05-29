package com.project.fakegps;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedHook implements IXposedHookLoadPackage {
    
    private static Context appContext;
    private static double fakeLat = 0;
    private static double fakeLng = 0;
    private static boolean isMocking = false;
    private static long lastCacheTime = 0;
    private static final long CACHE_DURATION = 3000; // Cache 3 detik agar aplikasi target tidak lag

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        // Jangan nge-hook sistem inti Android dan aplikasi FakeGPS itu sendiri
        if (lpparam.packageName.equals("android") || lpparam.packageName.equals("com.project.fakegps")) return;

        // Dapatkan konteks (Context) aplikasi target saat ini secara diam-diam
        try {
            XposedHelpers.findAndHookMethod(
                "android.app.ContextImpl", 
                lpparam.classLoader, 
                "setOuterContext", 
                Context.class, 
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (appContext == null) {
                            appContext = (Context) param.args[0];
                        }
                    }
                }
            );
        } catch (Throwable t) {
            XposedBridge.log("FakeGPS - Gagal mendapatkan Context: " + t.getMessage());
        }

        // 1. BYPASS ANTI-TUYUL (Sembunyikan status mock)
        try {
            XposedHelpers.findAndHookMethod(Location.class, "isFromMockProvider", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(false);
                }
            });
        } catch (Throwable t) {}

        // 2. CEGAT LOKASI (OVERWRITE TITIK GPS)
        try {
            XposedHelpers.findAndHookMethod(Location.class, "getLatitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    updateFakeLocation();
                    if (isMocking) param.setResult(fakeLat);
                }
            });
            XposedHelpers.findAndHookMethod(Location.class, "getLongitude", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    updateFakeLocation();
                    if (isMocking) param.setResult(fakeLng);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("FakeGPS - Gagal hook getLatitude/Longitude: " + t.getMessage());
        }
    }

    // Fungsi untuk berkomunikasi dengan aplikasi utama FakeGPS
    private static void updateFakeLocation() {
        if (appContext == null) return;
        
        long now = System.currentTimeMillis();
        // Gunakan cache jika belum 3 detik agar HP tidak berat karena terlalu sering query
        if (now - lastCacheTime < CACHE_DURATION) return; 
        lastCacheTime = now;

        try {
            Uri uri = Uri.parse("content://com.project.fakegps.provider/location");
            Cursor cursor = appContext.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                fakeLat = cursor.getDouble(0);
                fakeLng = cursor.getDouble(1);
                isMocking = "start".equals(cursor.getString(2));
                cursor.close();
            }
        } catch (Exception e) {
            // Abaikan jika gagal (artinya FakeGPS belum berjalan)
        }
    }
}
