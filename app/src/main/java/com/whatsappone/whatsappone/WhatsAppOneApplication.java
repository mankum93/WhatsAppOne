package com.whatsappone.whatsappone;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;
import com.whatsappone.whatsappone.database.ContactsDbHelper;

/**
 * Created by DJ on 10/14/2017.
 */

public class WhatsAppOneApplication extends Application {

    // Use a single instance of Db throughout the Application. Don't forget to close
    // it.
    public static ContactsDbHelper dbHelper;
    public static SQLiteDatabase dbInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Stetho
        Stetho.initializeWithDefaults(this);

        // To enable vector images support through the support library
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        // Enable/disable components depending on the API level.

        // For API level >= 19, enable the WhatsAppNotificationListenerService
        PackageManager pm  = getApplicationContext().getPackageManager();
        ComponentName componentName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            componentName = new ComponentName("com.whatsappone.whatsappone",
                    "com.whatsappone.whatsappone.services.WhatsAppNotificationListenerService");
        }
        else{
            componentName = new ComponentName("com.whatsappone.whatsappone",
                    "com.whatsappone.whatsappone.services.WhatsAppNotificationAccessibilityService");
        }
        // Enable it.
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        dbHelper = new ContactsDbHelper(this.getApplicationContext());
        dbInstance = dbHelper.getWritableDatabase();
    }
}
