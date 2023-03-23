package com.centrale.smartmove.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.centrale.smartmove.R;
//import com.centrale.smartmove.debugus4.DebugUS4;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ArrayList<String> permissions = new ArrayList<>();
    private Context context;

    public static Context getContext() {
        return context;
    }

    /**
     * Method that displays the application's menu view or the CGU view if it has never been accepted.
     * @param savedInstanceState
     */

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean DEBUG_SCREEN = true;
        permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        permissions.add(Manifest.permission.VIBRATE);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        //loop through permissions and check if they are granted, if not, print a message
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("Permission not granted: " + permission);
            }
        }
        requestPermissions(permissions.toArray(new String[0]), 1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.smartmove_intext),Context.MODE_PRIVATE);
        int CGU_accepted = sharedPref.getInt(getString(R.string.acceptedCGUtext), 0);
        if (CGU_accepted == 0) {
            Intent intent = new Intent(this, CGUActivity.class);
            startActivity(intent);
        } /*else {
            /*if (DEBUG_SCREEN) {
                Intent intent = new Intent(this, DebugUS4.class);
                startActivity(intent);
                return;
            }*/
            Intent intent = new Intent(this,DashboardActivity.class);
            startActivity(intent);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("My Notification", "My Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}