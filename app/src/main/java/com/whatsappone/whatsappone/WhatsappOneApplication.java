package com.whatsappone.whatsappone;

import android.app.Application;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Created by DJ on 10/14/2017.
 */

public class WhatsappOneApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable/disable components depending on the API level.

        // For API level >= 18, enable the WhatsappNotificationListenerService
        PackageManager pm  = getApplicationContext().getPackageManager();
        ComponentName componentName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            componentName = new ComponentName("com.whatsappone.whatsappone",
                    "com.whatsappone.whatsappone.services.WhatsappNotificationListenerService");
        }
        else{
            componentName = new ComponentName("com.whatsappone.whatsappone",
                    "com.whatsappone.whatsappone.services.WhatsappNotificationAccessibilityService");
        }
        // Enable it.
        pm.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }
}
