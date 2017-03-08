package net.harimurti.safecharging.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.stericson.RootShell.*;

import net.harimurti.safecharging.R;
import net.harimurti.safecharging.engine.*;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "SafeCharging";
    private Context context;
    private Handler backgroundUpdate;
    private boolean isRunningUpdate;
    private boolean isSupported;
    private boolean doubleBackToExitPressedOnce = false;
    SharedPreferences preferences;
    SharedPreferences.Editor prefEditor;
    TextView lbLevel, lbHealth, lbStatus, lbService;
    Switch swUsbPower, swOverLevel, swOverAll;
    SeekBar seekBarMax, seekBarMin;
    final static int seekBarMaxValue = 30;
    public static final int defaultMaxLevel = 90;
    public static final int defaultMinLevel = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefEditor = preferences.edit();

        lbLevel = (TextView) findViewById(R.id.textView5);
        lbHealth = (TextView) findViewById(R.id.textView8);
        lbStatus = (TextView) findViewById(R.id.textView11);
        lbService = (TextView) findViewById(R.id.textView14);
        swOverLevel = (Switch) findViewById(R.id.switch1);
        swUsbPower = (Switch) findViewById(R.id.switch2);
        swOverAll = (Switch) findViewById(R.id.switch3);
        seekBarMax = (SeekBar) findViewById(R.id.seekBar1);
        seekBarMin = (SeekBar) findViewById(R.id.seekBar2);

        swOverLevel.setChecked(preferences.getBoolean("stopOnLevel", false));
        swUsbPower.setChecked(preferences.getBoolean("stopOnUsb", false));
        swOverAll.setChecked(preferences.getBoolean("stopOnOver", false));

        isSupported = Charging.isSupported();
        if (!RootShell.isAccessGiven()) {
            //android.os.Process.killProcess(android.os.Process.myPid());
            isSupported = false;
        }

        swOverLevel.setEnabled(isSupported);
        swUsbPower.setEnabled(isSupported);
        swOverAll.setEnabled(isSupported);

        swOverLevel.setOnClickListener(this);
        swUsbPower.setOnClickListener(this);
        swOverAll.setOnClickListener(this);

        if (preferences.getInt("maxLevel", 0) == 0) {
            prefEditor.putInt("maxLevel", defaultMaxLevel);
            prefEditor.apply();
        }
        if (preferences.getInt("minLevel", 0) == 0) {
            prefEditor.putInt("minLevel", defaultMinLevel);
            prefEditor.apply();
        }

        seekBarMax.setMax(30);
        setTextMaxLevel(preferences.getInt("maxLevel", defaultMaxLevel));
        seekBarMax.setProgress(seekBarMaxValue - (100 - preferences.getInt("maxLevel", defaultMaxLevel)));

        seekBarMin.setMax(preferences.getInt("maxLevel", defaultMaxLevel) - 10);
        setTextUsbLevel(preferences.getInt("minLevel", defaultMinLevel), preferences.getInt("maxLevel", defaultMaxLevel));
        seekBarMin.setProgress(preferences.getInt("minLevel", defaultMinLevel));

        seekBarMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= 30){
                    progress = (100 - 30) + progress;
                }

                prefEditor.putInt("maxLevel", progress);
                prefEditor.apply();

                setTextMaxLevel(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        seekBarMin.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= 10){
                    progress = 10 + progress;
                }

                prefEditor.putInt("minLevel", progress);
                prefEditor.apply();

                setTextUsbLevel(progress, preferences.getInt("maxLevel", defaultMaxLevel));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isRunningUpdate) {
            isRunningUpdate = true;
            backgroundUpdate = new Handler();
            backgroundUpdate.post(runnableUpdate);
        }
        serviceManager();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRunningUpdate = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch1:
                prefEditor.putBoolean("stopOnLevel", swOverLevel.isChecked());
                break;

            case R.id.switch2:
                prefEditor.putBoolean("stopOnUsb", swUsbPower.isChecked());
                break;

            case R.id.switch3:
                prefEditor.putBoolean("stopOnOver", swOverAll.isChecked());
                break;
        }
        prefEditor.apply();
        serviceManager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_restart) {
            if (isSupported && (swOverLevel.isChecked() || swUsbPower.isChecked() || swOverAll.isChecked())) {
                BatteryStatus Battery = new BatteryStatus(context);
                String source = Battery.Plugged;
                if (!source.contains("Unknown")) {
                    Intent service = new Intent(context, BatteryService.class);
                    if (isServiceRunning(BatteryService.class))
                        context.stopService(service);
                    context.startService(service);
                }
            } else {
                Toast toast = Toast.makeText(this, R.string.toast_nomonitor, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            return true;
        } else if (id == R.id.action_reset) {
            if (isSupported) {
                if (isServiceRunning(BatteryService.class)) {
                    context.stopService(new Intent(context, BatteryService.class));
                }

                Charging.setEnabled(true);

                Toast toast = Toast.makeText(this, R.string.toast_reset, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            return true;
        } else if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.please_visit)
                    .setTitle(getString(R.string.app_name) + " v" + getVersion());

            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://harimurti.net"));
                    startActivity(i);
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        } else if (id == R.id.action_exit) {
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    private Runnable runnableUpdate = new Runnable() {
        @Override
        public void run() {
            BatteryStatus Battery = new BatteryStatus(context);

            lbLevel.setText(String.format(Locale.US, "%d", Battery.Level));
            lbHealth.setText(Battery.Health);
            if (Battery.isCharging)
                lbStatus.setText(Battery.Plugged);
            else
                lbStatus.setText(Battery.Status);

            if (isSupported) {
                if (isServiceRunning(BatteryService.class)) {
                    lbService.setText(R.string.label_monitor);
                } else {
                    lbService.setText(R.string.label_notrunning);
                }
            } else {
                lbService.setText(R.string.label_notsupported);
            }

            if (isRunningUpdate) backgroundUpdate.postDelayed(this, 1000);
        }
    };

    private void setTextMaxLevel(int value) {
        String textToDisplay = String.format(getString(R.string.sw_overlevel_custom), value)
                + getString(R.string.label_persen);
        swOverLevel.setText(textToDisplay);
        int min = preferences.getInt("minLevel", defaultMinLevel);
        setTextUsbLevel(min, value);
        seekBarMin.setMax(value - 10);
        if (value < min) {
            seekBarMin.setProgress(value - 10);
            prefEditor.putInt("minLevel", value - 10);
            prefEditor.apply();
        }
    }

    private void setTextUsbLevel(int min, int max) {
        String textToDisplay = String.format(getString(R.string.sw_usb_custom) + " %d-%d", min, max)
                + getString(R.string.label_persen);
        swUsbPower.setText(textToDisplay);
    }

    private void serviceManager() {
        BatteryStatus Battery = new BatteryStatus(context);
        if (isSupported && !Battery.Plugged.contains("Unknown")) {
            if (preferences.getBoolean("stopOnUsb", false) ||
                    preferences.getBoolean("stopOnLevel", false) ||
                    preferences.getBoolean("stopOnOver", false)) {
                if (!isServiceRunning(BatteryService.class)) {
                    context.startService(new Intent(context, BatteryService.class));
                }
            } else {
                context.stopService(new Intent(context, BatteryService.class));
            }
        } else {
            context.stopService(new Intent(context, BatteryService.class));
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public String getVersion() {
        String v = "1.0";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return v;
    }
}
