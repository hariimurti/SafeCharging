package net.harimurti.safecharging.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
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
    private ConfigManager config;
    TextView lbLevel, lbHealth, lbStatus, lbService, lbOverLevel, lbUsbLevel;
    Switch swUsbPower, swOverLevel, swOverAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = this;
        config = new ConfigManager(this);

        lbLevel = (TextView) findViewById(R.id.textView5);
        lbHealth = (TextView) findViewById(R.id.textView8);
        lbStatus = (TextView) findViewById(R.id.textView11);
        lbService = (TextView) findViewById(R.id.textView14);
        lbOverLevel = (TextView) findViewById(R.id.textView15);
        lbUsbLevel = (TextView) findViewById(R.id.textView17);

        swOverLevel = (Switch) findViewById(R.id.switch1);
        swUsbPower = (Switch) findViewById(R.id.switch2);
        swOverAll = (Switch) findViewById(R.id.switch3);

        swOverLevel.setChecked(config.getBoolean("stopOnLevel"));
        swUsbPower.setChecked(config.getBoolean("stopOnUsb"));
        swOverAll.setChecked(config.getBoolean("stopOnOver"));

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
                config.setBoolean("stopOnLevel", swOverLevel.isChecked());
                break;

            case R.id.switch2:
                config.setBoolean("stopOnUsb", swUsbPower.isChecked());
                break;

            case R.id.switch3:
                config.setBoolean("stopOnOver", swOverAll.isChecked());
                break;

            case R.id.setbatt:
                CustomDialog.showDialogMax(context);
                break;

            case R.id.setbatt1:
                CustomDialog.showDialogMinMax(context);
                break;
        }
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
            CustomDialog.showDialogAbout(context);
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

            lbOverLevel.setText(getString(R.string.sw_overlevel_custom) + " > " +
                    Integer.toString(config.getInteger("maxLevel")) + "%");

            if (config.getBoolean("usbDisableFull")) {
                lbUsbLevel.setText(R.string.sw_usb);
            } else {
                lbUsbLevel.setText(getString(R.string.sw_usb_custom) + " & Charge at Level " +
                        Integer.toString(config.getInteger("minUsbLevel")) + "–" +
                        Integer.toString(config.getInteger("maxUsbLevel")) + "%");
            }

            if (isRunningUpdate) backgroundUpdate.postDelayed(this, 1000);
        }
    };

    private void serviceManager() {
        BatteryStatus Battery = new BatteryStatus(context);
        if (isSupported && !Battery.Plugged.contains("Unknown")) {
            if (config.getBoolean("stopOnUsb") ||
                    config.getBoolean("stopOnLevel") ||
                    config.getBoolean("stopOnOver")) {
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
}
