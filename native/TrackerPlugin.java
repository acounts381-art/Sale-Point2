package com.ptc.salepoint;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Tracker")
public class TrackerPlugin extends Plugin {

    private boolean granted(String p) {
        return ContextCompat.checkSelfPermission(getContext(), p) == PackageManager.PERMISSION_GRANTED;
    }

    @PluginMethod
    public void status(PluginCall call) {
        Context x = getContext();
        JSObject r = new JSObject();
        r.put("fine", granted(Manifest.permission.ACCESS_FINE_LOCATION));
        r.put("background", Build.VERSION.SDK_INT < 29 || granted(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
        r.put("notifications", Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS));
        PowerManager pm = (PowerManager) x.getSystemService(Context.POWER_SERVICE);
        r.put("battery", pm != null && pm.isIgnoringBatteryOptimizations(x.getPackageName()));
        call.resolve(r);
    }

    @PluginMethod
    public void askLocation(PluginCall call) {
        ActivityCompat.requestPermissions(getActivity(),
            new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 9001);
        call.resolve();
    }

    @PluginMethod
    public void askBackground(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 30) {
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } else if (Build.VERSION.SDK_INT == 29) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 9002);
        }
        call.resolve();
    }

    @PluginMethod
    public void askNotifications(PluginCall call) {
        if (Build.VERSION.SDK_INT >= 33) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 9003);
        }
        call.resolve();
    }

    @PluginMethod
    public void askBattery(PluginCall call) {
        try {
            Intent i = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getContext().getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
        } catch (Exception e) { }
        call.resolve();
    }

    @PluginMethod
    public void start(PluginCall call) {
        ContextCompat.startForegroundService(getContext(), new Intent(getContext(), TrackerService.class));
        call.resolve();
    }

    @PluginMethod
    public void fixNow(PluginCall call) {
        Intent i = new Intent(getContext(), TrackerService.class);
        i.setAction("FIX");
        ContextCompat.startForegroundService(getContext(), i);
        call.resolve();
    }

    @PluginMethod
    public void points(PluginCall call) {
        Double since = call.getDouble("since", 0.0);
        JSObject r = new JSObject();
        r.put("points", TrackerService.read(getContext(), since == null ? 0L : since.longValue()));
        call.resolve(r);
    }
}
