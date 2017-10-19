package com.whatsappone.whatsappone.services;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.BuildConfig;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.whatsappone.whatsappone.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import model.WhatsAppMessage;

/**
 * Created by DJ on 10/14/2017.
 */

/**
 * A Service to listen on to WhatsApp messages for API level < 19.
 *
 * Note: This service uses reflection to retrieve the text - a solution developers
 * had to resort to due to unavailability of another mechanism.
 */
public class WhatsAppNotificationAccessibilityService extends AccessibilityService {


    private static final String TAG = "AccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event != null && event.getPackageName().toString().contains("com.whatsapp")) {

            if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
                return;
            }

            Notification notification = (Notification) event.getParcelableData();

            try {
                WhatsAppMessage message = buildMessage(notification);



            } catch (Exception npe) {
                Toast.makeText(this, "Could not retrive message", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Restart in case we get killed after returning from here
        return START_STICKY;
    }

    @Override
    public void onInterrupt() {

    }


    // Based on Jon C. Hammer's implementation:
    // http://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
    public WhatsAppMessage buildMessage(Notification notification) throws IllegalAccessException, NoSuchFieldException {

        // Message to be built
        WhatsAppMessage message = new WhatsAppMessage();

        // Temp holder for various text properties we retrieve
        List<String> texts = new ArrayList<>();

        RemoteViews views = getView(notification);
        if (views == null) {
            return null;
        }
        Field field = views.getClass().getDeclaredField("mActions");
        field.setAccessible(true);
        ArrayList<Parcelable> actions = (ArrayList<Parcelable>) field.get(views);

        for (Parcelable p : actions) {
            Parcel parcel = Parcel.obtain();
            p.writeToParcel(parcel, 0);
            parcel.setDataPosition(0);
            int tag = parcel.readInt();
            if (tag != 2) {
                continue;
            }
            parcel.readInt();
            String methodName = parcel.readString();
            if (methodName == null) continue;
            else if (methodName.equals("setText")) {
                parcel.readInt();
                texts.add(TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(parcel) + "");
            }
            else if (methodName.equals("setTime")){
                // Parameter type (5 = Long)
                parcel.readInt();

                long time = parcel.readLong();
                message.setMessageTime(time);
            }
            parcel.recycle();
        }

        int i;

        if(BuildConfig.DEBUG){

            for(i = 0; i < texts.size(); i++){
                Log.d(TAG, i + "th element: " + texts.get(i) + "\n");
            }
        }

        for(i = 0; i < texts.size(); i++){
            switch (i){
                case 0:
                    // 0th element is the Sender
                    message.setSenderName(texts.get(0));
                    break;
                case 1:
                    // 1st element is the Ticker text
                    break;
                case 2:
                    // 2nd element is the main message.
                    message.setSenderName(texts.get(2));
                    break;
            }
        }


        return message;
    }

    public RemoteViews getView(Notification notif) {
        RemoteViews views = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            views = notif.bigContentView;
        } else {
            views = notif.contentView;
        }
        return views;
    }
}
