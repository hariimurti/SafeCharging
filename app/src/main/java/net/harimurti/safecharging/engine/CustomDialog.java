package net.harimurti.safecharging.engine;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import net.harimurti.safecharging.R;

public class CustomDialog {
    private static int seekBarMaxValue = 30;

    public static void showDialogMax(Context context) {
        final ConfigManager config = new ConfigManager(context);

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_max);
        dialog.setTitle("Charging Options");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textMaximumLevel);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBarMaximumLevel);

        int maxValue = config.getInteger("max_level");
        if (maxValue == 0) {
            maxValue = config.defaultMaxLevel;
            config.setInteger("max_level", maxValue);
        }
        config.setInteger("tmp_max_level", maxValue);

        seekBarMax.setMax(30);
        seekBarMax.setProgress(seekBarMaxValue - (100 - maxValue));
        textMax.setText(Integer.toString(maxValue) + "%");

        seekBarMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= 30){
                    progress = (100 - 30) + progress;
                }

                textMax.setText(Integer.toString(progress) + "%");
                config.setInteger("tmp_max_level", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        boolean isSet = config.getBoolean("switch_battery_full");
        Switch disableSwitch = (Switch) dialog.findViewById(R.id.switchBatteryFull);
        disableSwitch.setChecked(isSet);
        seekBarMax.setEnabled(!isSet);
        config.setBoolean("tmp_switch_battery_full", isSet);

        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.setBoolean("tmp_switch_battery_full", isChecked);
                seekBarMax.setEnabled(!isChecked);
            }
        });

        Button dialogSave = (Button) dialog.findViewById(R.id.btnSave1);
        Button dialogCancel = (Button) dialog.findViewById(R.id.btnCancel1);

        dialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setInteger("max_level", config.getInteger("tmp_max_level"));
                config.setBoolean("switch_battery_full", config.getBoolean("tmp_switch_battery_full"));
                dialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialogMaxWindow(dialog);
    }

    public static void showDialogMinMax(final Context context) {
        final ConfigManager config = new ConfigManager(context);

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_minmax);
        dialog.setTitle("USB Options");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textMaxLevel);
        final TextView textMin = (TextView) dialog.findViewById(R.id.textMinLevel);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBarMaxLevel);
        final SeekBar seekBarMin = (SeekBar) dialog.findViewById(R.id.seekBarMinLevel);

        int maxValue = config.getInteger("max_usb");
        if (maxValue == 0) {
            maxValue = config.defaultMaxLevel;
            config.setInteger("max_usb", maxValue);
        }
        int minValue = config.getInteger("min_usb");
        if (minValue == 0) {
            minValue = config.defaultMinLevel;
            config.setInteger("min_usb", minValue);
        }
        config.setInteger("tmp_max_usb", maxValue);
        config.setInteger("tmp_min_usb", minValue);

        seekBarMax.setMax(30);
        seekBarMax.setProgress(seekBarMaxValue - (100 - maxValue));
        textMax.setText(Integer.toString(maxValue) + "%");

        seekBarMin.setMax(maxValue - 10);
        seekBarMin.setProgress(minValue);
        textMin.setText(Integer.toString(minValue) + "%");

        seekBarMax.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress <= 30){
                    progress = (100 - 30) + progress;
                }

                textMax.setText(Integer.toString(progress) + "%");
                config.setInteger("tmp_max_usb", progress);

                int minProgress = progress - 10;
                seekBarMin.setMax(minProgress);
                if (seekBarMin.getProgress() >= progress) {
                    seekBarMin.setProgress(minProgress);
                    textMin.setText(Integer.toString(minProgress) + "%");
                    config.setInteger("tmp_min_usb", minProgress);
                }
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
                textMin.setText(Integer.toString(progress) + "%");
                config.setInteger("tmp_min_usb", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        boolean isSet = config.getBoolean("switch_disable_usb");
        Switch disableSwitch = (Switch) dialog.findViewById(R.id.switchDisableUsb);
        disableSwitch.setChecked(isSet);
        seekBarMax.setEnabled(!isSet);
        seekBarMin.setEnabled(!isSet);
        config.setBoolean("tmp_switch_disable_usb", isSet);

        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.setBoolean("tmp_switch_disable_usb", isChecked);
                seekBarMax.setEnabled(!isChecked);
                seekBarMin.setEnabled(!isChecked);
            }
        });

        Button dialogSave = (Button) dialog.findViewById(R.id.btnSave2);
        Button dialogCancel = (Button) dialog.findViewById(R.id.btnCancel2);

        dialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setInteger("max_usb", config.getInteger("tmp_max_usb"));
                config.setInteger("min_usb", config.getInteger("tmp_min_usb"));
                config.setBoolean("switch_disable_usb", config.getBoolean("tmp_switch_disable_usb"));
                dialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialogMaxWindow(dialog);
    }

    public static void showDialogAbout(final Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_about);
        dialog.setTitle("About");

        TextView labelApp = (TextView) dialog.findViewById(R.id.textView26);
        labelApp.setText(context.getString(R.string.app_name) + " v" + getVersion(context));

        Button dialogVisit = (Button) dialog.findViewById(R.id.button5);
        Button dialogCancel = (Button) dialog.findViewById(R.id.button6);

        dialogVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openWebsite = new Intent(Intent.ACTION_VIEW);
                openWebsite.setData(Uri.parse("http://harimurti.net"));
                context.startActivity(openWebsite);
                dialog.dismiss();
            }
        });

        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialogMaxWindow(dialog);
    }

    private static void dialogMaxWindow(Dialog dialog) {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    public static String getVersion(Context context) {
        String v = "1.x";
        try {
            v = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return v;
    }
}
