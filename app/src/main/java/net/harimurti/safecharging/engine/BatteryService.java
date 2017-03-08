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

import net.harimurti.safecharging.R;
import net.harimurti.safecharging.activity.MainActivity;

public class BatteryService extends Service {
    private Handler backgroundService;
    private Context context;
    private boolean isRunning;
    private boolean setCharging;
    private int lastLevel = 0;
    private int logId = 0;
    SharedPreferences preferences;

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
            setCharging = true;

            backgroundService = new Handler();
            backgroundService.post(runnableService);

            Notifications.Show(context, 0, "Battery Service", getString(R.string.notif_startmonitor), false);
            Log.i(MainActivity.TAG, "BatteryService: starting service & monitor charging");
            /*Log.i(MainActivity.TAG, "BatteryService: stopOnLevel: " +
                    Boolean.toString(preferences.getBoolean("stopOnLevel", false)) +
                    ", maxLevel: " + Integer.toString(preferences.getInt("maxLevel", 90)) +
                    "%, stopOnUsb: " + Boolean.toString(preferences.getBoolean("stopOnUsb", false)) +
                    ", minLevel: " + Integer.toString(preferences.getInt("minLevel", 60)) +
                    ", stopOnOver: " + Boolean.toString(preferences.getBoolean("stopOnOver", false)));*/
        }
        if (!preferences.getBoolean("stopOnUsb", false) &&
                !preferences.getBoolean("stopOnLevel", false) &&
                !preferences.getBoolean("stopOnOver", false)) {
            this.stopSelf();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Notifications.Close(context, 0);
        Log.i(MainActivity.TAG, "BatteryService: stop service & reset charging to enable");
        Charging.setEnabled(true);
    }

    private Runnable runnableService = new Runnable() {
        @Override
        public void run() {
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
                        setCharging = true;
                        if (logId != 1) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_startcharging),
                                    context.getString(R.string.notif_minlevel), false);
                            Log.i(MainActivity.TAG, "BatteryService: condition: usb source + level <= "
                                    + Integer.toString(minLevel) + "% = allowed to charging");
                            logId = 1;
                        }
                    }

                    if (Battery.Level >= maxLevel) {
                        setCharging = false;
                        if (logId != 2) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_stopcharging),
                                    context.getString(R.string.notif_maxlevel), true);
                            Log.i(MainActivity.TAG, "BatteryService: condition: usb source + level >= " +
                                    Integer.toString(maxLevel) + "% = not allowed to charging");
                            logId = 2;
                        }
                    }
                } else if (preferences.getBoolean("stopOnLevel", false)) {
                    if (Battery.Level <= 10) {
                        setCharging = true;
                        if (logId != 3) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_startcharging),
                                    context.getString(R.string.notif_minlevel), false);
                            Log.i(MainActivity.TAG, "BatteryService: condition: " +
                                    " + level <= 10% = allowed to charging");
                            logId = 3;
                        }
                    }

                    if (Battery.Level >= maxLevel) {
                        setCharging = false;
                        if (logId != 4) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_stopcharging),
                                    context.getString(R.string.notif_maxlevel), true);
                            Log.i(MainActivity.TAG, "BatteryService: condition: " +
                                    Battery.Plugged.toLowerCase() +
                                    " + level >= " + Integer.toString(maxLevel) +
                                    "% = not allowed to charging");
                            logId = 4;
                        }
                    }
                }

                //Log.i(MainActivity.TAG, "BatteryService: set charging " + Boolean.toString(setCharging));
                Charging.setEnabled(setCharging);
            }

            if (preferences.getBoolean("stopOnOver", false) && Battery.Health.contains("Over")) {
                if (logId != 5) {
                    Notifications.Show(context, 1, context.getString(R.string.notif_stopcharging),
                            "Battery Health is " + Battery.Health, true);
                    Log.i(MainActivity.TAG, "BatteryService: condition: health " +
                            Battery.Health.toLowerCase() + "% = not allowed to charging");
                    logId = 5;
                }
                Charging.setEnabled(false);
            }

            lastLevel = Battery.Level;

            if (isRunning) {
                backgroundService.postDelayed(this, 1000);
            } else {
                stopSelf();
            }
        }
    };
}
