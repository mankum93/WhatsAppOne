package com.whatsappone.whatsappone.util;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.whatsappone.whatsappone.services.WhatsappNotificationListenerService;

/**
 * Created by DJ on 10/14/2017.
 */

public class Util {

    private static final String TAG = "Util";

    /**
     * Check if we have been granted access notifications from other Apps.
     *
     * Credit goes to @achep | https://stackoverflow.com/questions/20141727/check-if-user-has-granted-notificationlistener-access-to-my-app
     *
     * @param context Context
     * @return true if we have been granted access to it.
     */
    public static boolean haveNotificationAccessPermission(Context context){

        ComponentName cn = new ComponentName(context, WhatsappNotificationListenerService.class);
        String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(cn.flattenToString());

        return enabled;
    }

    public static void listAllNotificationExtraKeysAndValues(Bundle bundle){
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
    }
}
