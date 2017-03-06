package net.harimurti.safecharging.engine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import net.harimurti.safecharging.R;
import net.harimurti.safecharging.activity.MainActivity;

public class BatteryService extends Service {
    private Handler backgroundService;
    private Context context;
    private boolean isRunning;
    private boolean reachMaxLevel;
    private int timer = 1000;
    private int lastLevel = 0;
    SharedPreferences preferences;
    String lastLog;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!isRunning) {
            this.context = this;
            isRunning = true;
            reachMaxLevel = false;
            lastLog = "";

            backgroundService = new Handler();
            backgroundService.postDelayed(runnableService, timer);

            Toast toast = Toast.makeText(this, R.string.toast_monitor, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            Log.i(MainActivity.TAG, "BatteryService: starting service & monitor charging");
            /*Log.i(MainActivity.TAG, "BatteryService: stopOnLevel: " + Boolean.toString(preferences.getBoolean("stopOnLevel", false))
                    + ", maxLevel: " + Integer.toString(preferences.getInt("maxLevel", 90))
                    + "%, stopOnUsb: " + Boolean.toString(preferences.getBoolean("stopOnUsb", false))
                    + ", minLevel: " + Integer.toString(preferences.getInt("minLevel", 60))
                    + ", stopOnOver: " + Boolean.toString(preferences.getBoolean("stopOnOver", false)));*/
        }
        if (!preferences.getBoolean("stopOnUsb", false)
                && !preferences.getBoolean("stopOnLevel", false)
                && !preferences.getBoolean("stopOnOver", false)) {
            this.stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        //Toast.makeText(this, "SafeCharging service is stopped!, Toast.LENGTH_SHORT).show();
        Log.i(MainActivity.TAG, "BatteryService: stop service & reset charging to enable");
        if (!Charging.isEnabled())
            Charging.setEnabled(true);
    }

    private Runnable runnableService = new Runnable() {
        @Override
        public void run() {
            boolean stopCharge = false;
            String newLog = "";

            BatteryStatus Battery = new BatteryStatus(context);
            if (lastLevel != Battery.Level) {
                Log.i(MainActivity.TAG, "BatteryService: power: " + Battery.Plugged.toLowerCase() +
                        ", level: " + Integer.toString(Battery.Level) +
                        "%, health: " + Battery.Health.toLowerCase() +
                        ", status: " + Battery.Status.toLowerCase());

                int maxLevel = preferences.getInt("maxLevel", MainActivity.defaultMaxLevel);
                int minLevel = preferences.getInt("minLevel", MainActivity.defaultMinLevel);

                if (preferences.getBoolean("stopOnUsb", false) && Battery.Plugged.contains("USB")) {
                    if (Battery.Level <= minLevel) {
                        reachMaxLevel = false;
                        newLog = "condition: usb source + level <= " + Integer.toString(minLevel) +
                                "% = allowed to charging";
                    }

                    if (Battery.Level >= maxLevel) {
                        reachMaxLevel = true;
                        newLog = "condition: usb source + level >= " + Integer.toString(maxLevel) +
                                "% = not allowed to charging";
                    }

                    stopCharge = reachMaxLevel;
                } else {
                    if (preferences.getBoolean("stopOnLevel", false) && (Battery.Level >= maxLevel)) {
                        reachMaxLevel = true;
                        newLog = "condition: " + Battery.Plugged.toLowerCase() +
                                " + level >= " + Integer.toString(maxLevel) +
                                "% = not allowed to charging";
                    }

                    stopCharge = reachMaxLevel;
                }
                if (preferences.getBoolean("stopOnOver", false) && Battery.Health.contains("Over")) {
                    stopCharge = true;
                    newLog = "condition: health " + Battery.Health.toLowerCase() +
                            "% = not allowed to charging";
                }

                if (lastLog != newLog) {
                    Log.i(MainActivity.TAG, "BatteryService: " + newLog);
                }

                if (stopCharge) {
                    if (Charging.isEnabled()) {
                        //Log.i(MainActivity.TAG, "BatteryService: set charging false");
                        Charging.setEnabled(false);
                    }
                } else {
                    if (!Charging.isEnabled()) {
                        /*Log.i(MainActivity.TAG, "BatteryService: no valid condition: " +
                                    "set charging true");*/
                        Charging.setEnabled(true);
                    }
                }

                lastLevel = Battery.Level;
                lastLog = newLog;
            }

            if (isRunning) {
                backgroundService.postDelayed(this, timer);
            } else {
                stopSelf();
            }
        }
    };
}
