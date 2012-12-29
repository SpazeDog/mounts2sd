package com.spazedog.mounts2sd;


import android.app.Application;
import android.content.Context;


public class BaseApplication extends Application {

    private static Context context = null;

    public void onCreate(){
        super.onCreate();
        
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
    
    public static void setContext(Context arg0) {
    	if (context == null) {
    		context = arg0.getApplicationContext();
    	}
    }
}
