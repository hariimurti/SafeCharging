package net.harimurti.safecharging.engine;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
    final static int seekBarMaxValue = 30;
    public static final int defaultMaxLevel = 90;
    public static final int defaultMinLevel = 60;

    public static void showDialogMax(Context context) {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor prefEditor = preferences.edit();

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_max);
        dialog.setTitle("Config All Charger");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textView21);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBar1);

        int maxValue = preferences.getInt("maxLevel", 0);
        if (maxValue == 0) {
            maxValue = defaultMaxLevel;
            prefEditor.putInt("maxLevel", defaultMaxLevel);
        }
        prefEditor.putInt("maxLevel-tmp", maxValue);
        prefEditor.apply();

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
                prefEditor.putInt("maxLevel-tmp", progress);
                prefEditor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        Button dialogSave = (Button) dialog.findViewById(R.id.button1);
        Button dialogCancel = (Button) dialog.findViewById(R.id.button2);

        dialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefEditor.putInt("maxLevel", preferences.getInt("maxLevel-tmp", defaultMaxLevel));
                prefEditor.apply();
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
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor prefEditor = preferences.edit();

        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_minmax);
        dialog.setTitle("Config for USB Source");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textView23);
        final TextView textMin = (TextView) dialog.findViewById(R.id.textView25);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBar2);
        final SeekBar seekBarMin = (SeekBar) dialog.findViewById(R.id.seekBar3);

        int maxValue = preferences.getInt("maxUsbLevel", 0);
        if (maxValue == 0) {
            maxValue = defaultMaxLevel;
            prefEditor.putInt("maxUsbLevel", defaultMaxLevel);
        }
        int minValue = preferences.getInt("minUsbLevel", 0);
        if (minValue == 0) {
            minValue = defaultMinLevel;
            prefEditor.putInt("minUsbLevel", defaultMinLevel);
        }
        prefEditor.putInt("maxUsbLevel-tmp", maxValue);
        prefEditor.putInt("minUsbLevel-tmp", minValue);
        prefEditor.apply();

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
                prefEditor.putInt("maxUsbLevel-tmp", progress);

                int minProgress = progress - 10;
                seekBarMin.setMax(minProgress);
                if (seekBarMin.getProgress() >= progress) {
                    seekBarMin.setProgress(minProgress);
                    textMin.setText(Integer.toString(minProgress) + "%");
                    prefEditor.putInt("minUsbLevel-tmp", minProgress);
                }
                prefEditor.apply();
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
                prefEditor.putInt("minUsbLevel-tmp", progress);
                prefEditor.apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        boolean isSet = preferences.getBoolean("usbDisableFull", false);
        Switch disableSwitch = (Switch) dialog.findViewById(R.id.switch4);
        disableSwitch.setChecked(isSet);
        prefEditor.putBoolean("usbDisableFull-tmp", isSet);
        prefEditor.apply();

        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefEditor.putBoolean("usbDisableFull-tmp", isChecked);
                prefEditor.apply();
            }
        });

        Button dialogSave = (Button) dialog.findViewById(R.id.button3);
        Button dialogCancel = (Button) dialog.findViewById(R.id.button4);

        dialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prefEditor.putInt("maxUsbLevel", preferences.getInt("maxUsbLevel-tmp", defaultMaxLevel));
                prefEditor.putInt("minUsbLevel", preferences.getInt("minUsbLevel-tmp", defaultMinLevel));
                prefEditor.putBoolean("usbDisableFull", preferences.getBoolean("usbDisableFull-tmp", false));
                prefEditor.apply();
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
}
