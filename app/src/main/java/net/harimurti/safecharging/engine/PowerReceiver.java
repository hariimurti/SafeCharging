package net.harimurti.safecharging.engine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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

        if (!source.contains("Unknown")) {
            Log.i(MainActivity.TAG, "PowerReceiver: " + source);
            PreService.Start(context);
        } else {
            PreService.Stop(context);
        }
    }
}
