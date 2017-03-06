package net.harimurti.safecharging.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import net.harimurti.safecharging.activity.MainActivity;

public class PowerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /* because in this receiver is late to detect usb, so we not gonna use it
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = (chargePlug == BatteryManager.BATTERY_PLUGGED_USB);
        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);*/

        BatteryStatus Battery = new BatteryStatus(context);
        String source = Battery.Plugged;
        Log.i(MainActivity.TAG, "PowerReceiver: " + source);

        Intent background = new Intent(context, BatteryService.class);
        if (!source.contains("Unknown")) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (preferences.getBoolean("stopOnUsb", false)
                    || preferences.getBoolean("stopOnLevel", false)
                    || preferences.getBoolean("stopOnOver", false)) {
                context.startService(background);
            }
        } else {
            context.stopService(background);
        }
    }
}
