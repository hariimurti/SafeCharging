package net.harimurti.safecharging.engine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ConfigManager {
    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;
    int defaultMaxLevel = 90;
    int defaultMinLevel = 60;

    public ConfigManager(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }

    public int getInteger(String key) {
        int defValue = 0;
        if (key.toLowerCase().contains("max"))
            defValue = defaultMaxLevel;
        else if (key.toLowerCase().contains("min"))
            defValue = defaultMinLevel;

        return preferences.getInt(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        prefEditor = preferences.edit();
        prefEditor.putBoolean(key, value);
        prefEditor.apply();
    }

    public void setInteger(String key, int value) {
        prefEditor = preferences.edit();
        prefEditor.putInt(key, value);
        prefEditor.apply();
    }
}
