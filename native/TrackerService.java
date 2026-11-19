package com.ptc.salepoint;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;

public class TrackerService extends Service implements LocationListener {
    private static final String CH = "tracker";
    private static long lastSaved = 0;
    private LocationManager lm;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onCreate() {
        super.onCreate();
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CH, "Tracking", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(c);
        }
        Notification n = new NotificationCompat.Builder(this, CH)
            .setContentTitle("Sale Point")
            .setContentText("Location tracking 6 AM - 8 PM")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true).build();
        if (Build.VERSION.SDK_INT >= 29) startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        else startForeground(1, n);
        subscribe();
    }

    private void subscribe() {
        try {
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 0, this);
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 60000, 0, this);
        } catch (SecurityException e) { }
    }

    @Override public int onStartCommand(Intent i, int f, int s) {
        if (i != null && "FIX".equals(i.getAction())) {
            try {
                if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) lm.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, Looper.getMainLooper());
                if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) lm.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, Looper.getMainLooper());
            } catch (SecurityException e) { }
        }
        return START_STICKY;
    }

    @Override public void onDestroy() {
        try { lm.removeUpdates(this); } catch (SecurityException e) { }
        super.onDestroy();
    }

    private static boolean inHours() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h >= 6 && h < 20;
    }

    @Override public void onLocationChanged(Location l) {
        if (!inHours()) return;
        long now = System.currentTimeMillis();
        if (now - lastSaved < 20000) return;
        lastSaved = now;
        save(this, now, l.getLatitude(), l.getLongitude(), l.getAccuracy());
    }

    @Override public void onStatusChanged(String p, int s, Bundle e) { }
    @Override public void onProviderEnabled(String p) { subscribe(); }
    @Override public void onProviderDisabled(String p) { }

    static synchronized void save(Context c, long t, double lat, double lng, float acc) {
        try {
            FileWriter w = new FileWriter(new File(c.getFilesDir(), "points.jsonl"), true);
            w.write("{\"t\":" + t + ",\"lat\":" + lat + ",\"lng\":" + lng + ",\"acc\":" + acc + "}\n");
            w.close();
        } catch (Exception e) { }
    }

    static synchronized JSArray read(Context c, long since) {
        JSArray a = new JSArray();
        try {
            File f = new File(c.getFilesDir(), "points.jsonl");
            if (!f.exists()) return a;
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line;
            while ((line = r.readLine()) != null) {
                try {
                    JSObject o = new JSObject(line);
                    if (o.getLong("t") > since) a.put(o);
                } catch (Exception e) { }
            }
            r.close();
        } catch (Exception e) { }
        return a;
    }
}
