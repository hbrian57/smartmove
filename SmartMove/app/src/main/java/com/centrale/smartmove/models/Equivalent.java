package com.centrale.smartmove.models;


import static android.app.PendingIntent.getActivity;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.centrale.smartmove.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Equivalent {

    //Attributes------------------------------------------------------------------------------------

    private String sentenceEq;
    private double ratioEq;
    private int imageID;

    //Constructors----------------------------------------------------------------------------------

    public Equivalent(){
        this.sentenceEq="repas végétariens";
        this.ratioEq= 1.961;
        this.imageID=  R.drawable.repavege;
    }

    public Equivalent(String sentence,double ratio, int imageRessourceID){
        this.sentenceEq=sentence;
        this.ratioEq=ratio;
        this.imageID=imageRessourceID;
    }

//Getters and setters---------------------------------------------------------------------------

    public double getRatioEq() {
        return ratioEq;
    }

    public String getSentenceEq() {
        return sentenceEq;
    }

    public int getImageID() {
        return imageID;
    }


}
