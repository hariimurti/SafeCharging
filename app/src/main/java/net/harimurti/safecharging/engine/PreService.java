package net.harimurti.safecharging.engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import net.harimurti.safecharging.activity.MainActivity;

public class PreService {
    private static Class<?> serviceClass = BatteryService.class;

    public static boolean Start(Context context) {
        boolean isRunning = false;
        if (!isServiceRunning(context)) {
            BatteryStatus Battery = new BatteryStatus(context);
            if (Charging.isSupported() && !Battery.Plugged.contains("Unknown")) {
                ConfigManager config = new ConfigManager(context);
                if (config.getBoolean("stopOnUsb") ||
                        config.getBoolean("stopOnLevel") ||
                        config.getBoolean("stopOnOver")) {
                    Intent background = new Intent(context, serviceClass);
                    context.startService(background);
                }
            }
            isRunning = isServiceRunning(context);
            if (isRunning)
                Log.i(MainActivity.TAG, "PreService: starting service");
            else
                Log.i(MainActivity.TAG, "PreService: can't start service");
        } else {
            Log.i(MainActivity.TAG, "PreService: service already running");
        }

        return isRunning;
    }

    public static void Stop(Context context) {
        if (isServiceRunning(context)) {
            Log.i(MainActivity.TAG, "PreService: stoping service");
            Intent background = new Intent(context, serviceClass);
            context.stopService(background);
        } else {
            Log.i(MainActivity.TAG, "PreService: no service is running");
        }
    }

    public static void Restart(Context context) {
        Log.i(MainActivity.TAG, "PreService: restart service");
        Intent background = new Intent(context, serviceClass);
        if (isServiceRunning(context)) {
            context.stopService(background);
        }
        BatteryStatus Battery = new BatteryStatus(context);
        if (Charging.isSupported() && !Battery.Plugged.contains("Unknown")) {
            ConfigManager config = new ConfigManager(context);
            if (config.getBoolean("stopOnUsb") ||
                    config.getBoolean("stopOnLevel") ||
                    config.getBoolean("stopOnOver")) {
                context.startService(background);
            }
        }
    }

    public static boolean isServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
