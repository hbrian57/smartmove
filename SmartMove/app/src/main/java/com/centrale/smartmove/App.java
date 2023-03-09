package com.centrale.smartmove;

import android.app.Application;
import android.content.Context;

public class App extends Application {
    private static Context appContext;

    @Override
    /**
     * Creation of the context.
     */
    public void onCreate() {
        super.onCreate();
        appContext = this;
    }

    /**
     * This method return the context of the App
     * @return the app context
     */
    public static Context getAppContext() {
        return appContext;
    }
}
