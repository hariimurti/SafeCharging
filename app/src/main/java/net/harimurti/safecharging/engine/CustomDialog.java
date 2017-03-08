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
        dialog.setTitle("Config All Charger");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textView21);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBar1);

        int maxValue = config.getInteger("maxLevel");
        if (maxValue == 0) {
            maxValue = config.defaultMaxLevel;
            config.setInteger("maxLevel", maxValue);
        }
        config.setInteger("maxLevel-tmp", maxValue);

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
                config.setInteger("maxLevel-tmp", progress);
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
                config.setInteger("maxLevel", config.getInteger("maxLevel-tmp"));
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
        dialog.setTitle("Config for USB Source");

        final TextView textMax = (TextView) dialog.findViewById(R.id.textView23);
        final TextView textMin = (TextView) dialog.findViewById(R.id.textView25);
        final SeekBar seekBarMax = (SeekBar) dialog.findViewById(R.id.seekBar2);
        final SeekBar seekBarMin = (SeekBar) dialog.findViewById(R.id.seekBar3);

        int maxValue = config.getInteger("maxUsbLevel");
        if (maxValue == 0) {
            maxValue = config.defaultMaxLevel;
            config.setInteger("maxUsbLevel", maxValue);
        }
        int minValue = config.getInteger("minUsbLevel");
        if (minValue == 0) {
            minValue = config.defaultMinLevel;
            config.setInteger("minUsbLevel", minValue);
        }
        config.setInteger("maxUsbLevel-tmp", maxValue);
        config.setInteger("minUsbLevel-tmp", minValue);

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
                config.setInteger("maxUsbLevel-tmp", progress);

                int minProgress = progress - 10;
                seekBarMin.setMax(minProgress);
                if (seekBarMin.getProgress() >= progress) {
                    seekBarMin.setProgress(minProgress);
                    textMin.setText(Integer.toString(minProgress) + "%");
                    config.setInteger("minUsbLevel-tmp", minProgress);
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
                config.setInteger("minUsbLevel-tmp", progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        boolean isSet = config.getBoolean("usbDisableFull");
        Switch disableSwitch = (Switch) dialog.findViewById(R.id.switch4);
        disableSwitch.setChecked(isSet);
        config.setBoolean("usbDisableFull-tmp", isSet);

        disableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                config.setBoolean("usbDisableFull-tmp", isChecked);
            }
        });

        Button dialogSave = (Button) dialog.findViewById(R.id.button3);
        Button dialogCancel = (Button) dialog.findViewById(R.id.button4);

        dialogSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                config.setInteger("maxUsbLevel", config.getInteger("maxUsbLevel-tmp"));
                config.setInteger("minUsbLevel", config.getInteger("minUsbLevel-tmp"));
                config.setBoolean("usbDisableFull", config.getBoolean("usbDisableFull-tmp"));
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
