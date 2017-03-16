package net.harimurti.safecharging.engine;

import android.content.Context;
import android.content.SharedPreferences;

public class ConfigManager {
    private SharedPreferences PrefSwitch;
    private SharedPreferences PrefValue;
    int defaultMaxLevel = 90;
    int defaultMinLevel = 60;

    public ConfigManager(Context context) {
        PrefSwitch = context.getSharedPreferences("Pref_Switch", Context.MODE_PRIVATE);
        PrefValue = context.getSharedPreferences("Pref_Value", Context.MODE_PRIVATE);
    }

    public boolean getBoolean(String key) {
        return PrefSwitch.getBoolean(key, false);
    }

    public void setBoolean(String key, boolean value) {
        PrefSwitch.edit().putBoolean(key, value).apply();
    }

    public int getInteger(String key) {
        int defValue = 0;
        if (key.toLowerCase().contains("max"))
            defValue = defaultMaxLevel;
        else if (key.toLowerCase().contains("min"))
            defValue = defaultMinLevel;

        return PrefValue.getInt(key, defValue);
    }

    public void setInteger(String key, int value) {
        PrefValue.edit().putInt(key, value).apply();
    }
}
