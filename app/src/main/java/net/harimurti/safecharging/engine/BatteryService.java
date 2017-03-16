package net.harimurti.safecharging.engine;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import net.harimurti.safecharging.R;
import net.harimurti.safecharging.activity.MainActivity;

public class BatteryService extends Service {
    private Handler backgroundService;
    private Context context;
    private boolean isRunning;
    private boolean setLooping;
    private boolean setCharging;
    private boolean lastState;
    private int lastLevel = 0;
    private int logId = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isRunning) {
            this.context = this;
            isRunning = true;
            setLooping = true;
            setCharging = true;

            backgroundService = new Handler();
            backgroundService.post(runnableService);

            //Notifications.Show(context, 0, "Battery Service", getString(R.string.notif_startmonitor), false);
            Notifications.Close(context, 0);
            Toast.makeText(context, R.string.toast_monitor, Toast.LENGTH_SHORT).show();
            Log.i(MainActivity.TAG, "BatteryService: starting service & monitor charging");
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
            ConfigManager config = new ConfigManager(context);
            BatteryStatus Battery = new BatteryStatus(context);

            if (lastLevel != Battery.Level) {
                Log.i(MainActivity.TAG, "BatteryService: power: " + Battery.Plugged.toLowerCase() +
                        ", level: " + Integer.toString(Battery.Level) +
                        "%, health: " + Battery.Health.toLowerCase() +
                        ", status: " + Battery.Status.toLowerCase());
            }

            if (config.getBoolean("switch_on_usb") && Battery.Plugged.contains("USB")) {
                if (config.getBoolean("switch_disable_usb")) {
                    setCharging = false;
                    setLooping = false;
                    if (logId != 1) {
                        Notifications.Show(context, 0, context.getString(R.string.notif_stopcharging),
                                context.getString(R.string.notif_usb_disable), true);
                        Log.i(MainActivity.TAG, "BatteryService: condition: usb source = not allowed to charging");
                        logId = 1;
                    }
                } else {
                    int minLevel = config.getInteger("min_usb");
                    if (Battery.Level <= minLevel) {
                        setCharging = true;
                        if (logId != 2) {
                            /*Notifications.Show(context, 0, context.getString(R.string.notif_startcharging),
                                    context.getString(R.string.notif_minlevel), false);*/
                            Notifications.Close(context, 0);
                            Toast.makeText(context, R.string.notif_startcharging, Toast.LENGTH_SHORT).show();
                            Log.i(MainActivity.TAG, "BatteryService: condition: usb source + level <= "
                                    + Integer.toString(minLevel) + "% = allowed to charging");
                            logId = 2;
                        }
                    }

                    int maxLevel = config.getInteger("max_usb");
                    if (Battery.Level >= maxLevel) {
                        setCharging = false;
                        if (logId != 3) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_maxlevel),
                                    context.getString(R.string.notif_usbmax), true);
                            Log.i(MainActivity.TAG, "BatteryService: condition: usb source + level >= " +
                                    Integer.toString(maxLevel) + "% = not allowed to charging");
                            logId = 3;
                        }
                    }
                }
            } else if (config.getBoolean("switch_on_level")) {
                if (config.getBoolean("switch_battery_full")) {
                    if (Battery.isBatteryFull) {
                        setCharging = false;
                        setLooping = false;
                        if (logId != 4) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_battfull),
                                    context.getString(R.string.notif_replug), true);
                            Log.i(MainActivity.TAG, "BatteryService: condition: " +
                                    context.getString(R.string.sw_battfull_label).toLowerCase() +
                                    " = not allowed to charging");
                            logId = 4;
                        }

                        Charging.setEnabled(false);
                    }
                } else {
                    if (Battery.Level >= config.getInteger("max_level")) {
                        setCharging = false;
                        setLooping = false;
                        if (logId != 6) {
                            Notifications.Show(context, 0, context.getString(R.string.notif_maxlevel),
                                    context.getString(R.string.notif_replug), true);
                            Log.i(MainActivity.TAG, "BatteryService: condition: " +
                                    Battery.Plugged.toLowerCase() +
                                    " + level >= " + Integer.toString(config.getInteger("max_level")) +
                                    "% = not allowed to charging");
                            logId = 6;
                        }
                    }
                }
            }

            if ((lastLevel != Battery.Level) && (lastState != setCharging)) {
                //Log.i(MainActivity.TAG, "BatteryService: set charging " + Boolean.toString(setCharging));
                Charging.setEnabled(setCharging);
            }

            if (config.getBoolean("switch_on_over") && Battery.Health.contains("Over")) {
                if (logId != 7 ) {
                    Notifications.Show(context, 0, context.getString(R.string.notif_stopcharging),
                            "Battery Health is " + Battery.Health, true);
                    Log.i(MainActivity.TAG, "BatteryService: condition: health " +
                            Battery.Health.toLowerCase() + "% = not allowed to charging");
                    logId = 7;
                }

                setCharging = false;
                Charging.setEnabled(false);
            } else if ((logId == 7) && Battery.Health.contains("Good")) {
                /*Notifications.Show(context, 0, context.getString(R.string.notif_startcharging),
                        context.getString(R.string.notif_healthgood), false);*/
                Notifications.Close(context, 0);
                Toast.makeText(context, R.string.notif_startcharging, Toast.LENGTH_SHORT).show();
                Log.i(MainActivity.TAG, "BatteryService: condition: health " +
                        Battery.Health.toLowerCase() + "% = allowed to charging");

                setCharging = true;
                Charging.setEnabled(true);
                logId = 0;
            }

            lastLevel = Battery.Level;
            lastState = setCharging;

            if (!config.getBoolean("switch_on_level") && !config.getBoolean("switch_on_usb") &&
                    !config.getBoolean("switch_on_over")) {

                Log.i(MainActivity.TAG, "BatteryService: kill service, " +
                        "because none of condition is set on");

                setLooping = false;
                stopSelf();
            }

            if (setLooping) {
                backgroundService.postDelayed(this, 1000);
            } else {
                Log.i(MainActivity.TAG, "BatteryService: stop looping service");
            }
        }
    };
}
