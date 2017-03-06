package net.harimurti.safecharging.engine;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryStatus {
    public boolean isCharging;
    public int Level;
    public String Plugged;
    public String Status;
    public String Health;

    public BatteryStatus(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, filter);

        Level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

        switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                isCharging = true;
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                isCharging = true;
                break;
            default:
                isCharging = false;
                break;
        }

        switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            case BatteryManager.BATTERY_PLUGGED_USB:
                Plugged = "USB Source";
                break;
            case BatteryManager.BATTERY_PLUGGED_AC:
                Plugged = "AC Charger";
                break;
            case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                Plugged = "Wireless Charger";
                break;
            default:
                Plugged = "Unknown Source";
                break;
        }

        if (!isCharging) {
            switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    Status = "Discharging";
                    break;
                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    Status = "Not Charging";
                    break;
                default:
                    Status = "Unknown Status";
                    break;
            }
        } else {
            Status = "Charging";
        }

        switch (batteryStatus.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            case BatteryManager.BATTERY_HEALTH_COLD:
                Health = "Cold";
                break;
            case BatteryManager.BATTERY_HEALTH_DEAD:
                Health = "Dead";
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                Health = "Good";
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                Health = "Over Voltage ( WARNING )";
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                Health = "Over Heat ( WARNING )";
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                Health = "Failure";
                break;
            default:
                Health = "Unknown";
                break;
        }
    }
}
