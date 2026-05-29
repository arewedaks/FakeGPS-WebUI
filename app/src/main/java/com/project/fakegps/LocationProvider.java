package com.project.fakegps;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class LocationProvider extends ContentProvider {
    public static final String AUTHORITY = "com.project.fakegps.provider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{"lat", "lng", "status"});
        Context context = getContext();
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("FakeGPS_Prefs", Context.MODE_PRIVATE);
            double lat = Double.longBitsToDouble(prefs.getLong("lat", Double.doubleToLongBits(-6.175392)));
            double lng = Double.longBitsToDouble(prefs.getLong("lng", Double.doubleToLongBits(106.827153)));
            String status = prefs.getString("status", "stop");
            cursor.addRow(new Object[]{lat, lng, status});
        }
        return cursor;
    }

    @Override public String getType(Uri uri) { return null; }
    @Override public Uri insert(Uri uri, ContentValues values) { return null; }
    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }
}
