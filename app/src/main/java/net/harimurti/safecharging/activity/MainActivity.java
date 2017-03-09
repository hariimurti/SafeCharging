package net.harimurti.safecharging.activity;

import android.content.Context;
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
        PreService.Start(context);
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

        if (isSupported && (swOverLevel.isChecked() || swUsbPower.isChecked() || swOverAll.isChecked())) {
            PreService.Start(context);
        } else {
            PreService.Stop(context);
        }
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
                PreService.Restart(context);
            } else {
                Toast toast = Toast.makeText(this, R.string.toast_nomonitor, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            return true;
        } else if (id == R.id.action_reset) {
            if (isSupported) {
                PreService.Stop(context);
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
                if (PreService.isServiceRunning(context)) {
                    lbService.setText(R.string.label_monitor);
                } else {
                    lbService.setText(R.string.label_notrunning);
                }
            } else {
                lbService.setText(R.string.label_notsupported);
            }

            if (config.getBoolean("batteryFull")) {
                lbOverLevel.setText(getString(R.string.sw_battfull_label));
            } else {
                lbOverLevel.setText(getString(R.string.sw_overlevel_custom) + " > " +
                        Integer.toString(config.getInteger("maxLevel")) + "%");
            }

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
}
