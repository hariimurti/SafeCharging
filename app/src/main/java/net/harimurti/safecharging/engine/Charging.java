package net.harimurti.safecharging.engine;

import android.util.Log;

import com.stericson.RootShell.RootShell;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.exceptions.RootDeniedException;

import net.harimurti.safecharging.activity.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeoutException;

public class Charging {
    private static String pathFile = "/sys/class/power_supply/battery/charging_enabled";

    public static boolean isEnabled() {
        String retval = "";

        try {
            FileInputStream inputStream = new FileInputStream(new File(pathFile));

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String receiveString;

            while ( (receiveString = bufferedReader.readLine()) != null ) {
                stringBuilder.append(receiveString);
            }

            inputStream.close();
            retval = stringBuilder.toString();
        }
        catch (FileNotFoundException e) {
            Log.e(MainActivity.TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(MainActivity.TAG, "Can't read file: " + e.toString());
        }

        return retval.contains("1");
    }

    public static boolean setEnabled(Boolean enabled) {
        boolean retval = false;
        //executeCommand("chmod 777 " + pathFile);

        if (enabled) {
            executeCommand("echo 1 > " + pathFile);
            if (isEnabled()) {
                Log.i(MainActivity.TAG, "Charging: Enable (Normal)");
                retval = true;
            }
        } else {
            executeCommand("echo 0 > " + pathFile);
            if (!isEnabled()) {
                Log.i(MainActivity.TAG, "Charging: Disable (Stop)");
                retval = true;
            }
        }

        //executeCommand("chmod 644 " + pathFile);
        //executeCommand("restorecon -F " + pathFile);

        if (!retval)
            Log.e(MainActivity.TAG, "Can't modified value!");

        return retval;
    }

    private static void executeCommand(String cmd) {
        Command command = new Command(0, cmd);
        try {
            RootShell.getShell(true).add(command);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Log.i("Execute", cmd);
        } catch (TimeoutException | RootDeniedException | IOException e) {
            Log.e(MainActivity.TAG, "RootShell: " + e.getMessage());
        }
    }
}
