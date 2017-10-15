package com.whatsappone.whatsappone.services;

import android.app.Notification;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;

import com.whatsappone.whatsappone.WhatsappOneApplication;
import com.whatsappone.whatsappone.database.ContactsDbHelper;

import model.Contact;
import model.WhatsAppMessage;

import static com.whatsappone.whatsappone.services.ChatHeadsService.ACTION_NEW_MESSAGE;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_NEW_MESSAGE;

/**
 * Created by DJ on 10/14/2017.
 */

/**
 * A Service that listens to Notification events, specifically that of WhatsApp messages
 * and show a floating window(a chat bubble) for the same.
 *
 * </br>
 *
 * Note: We use the standard Notification listening mechanism on API level >= 19. For less than
 * 19(18 actually but the field Notification.mExtras is not available so the info. too is not
 * and we are compelled to bump the required version to +1),
 * there is an AccessibilityService that tries fetching the messages from WhatsApp.
 */
@RequiresApi(19)
public class WhatsappNotificationListenerService extends NotificationListenerService {

    private SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the Single Db instance
        db = ((WhatsappOneApplication) this.getApplication()).dbInstance;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String packageName = sbn.getPackageName();

        // Only the WhatsApp messages are relevant
        if(packageName.contains("com.whatsapp")){

            WhatsAppMessage message = buildMessage(sbn);

            if(message != null){
                // Build a Contact
                Contact contact = buildContact(message);

                // Write it to the Database and update the UI.
                ContactsDbHelper.insertContactToDb(db, contact);
                ContactsDbHelper.insertMessageToDb(db, message);

                // Update the UI; Send a Broadcast
                Intent intent = new Intent(ACTION_NEW_MESSAGE);
                intent.putExtra(EXTRA_NEW_MESSAGE, message);
                LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        //...
    }

    private WhatsAppMessage buildMessage(StatusBarNotification statusBarNotification){

        Notification notification = statusBarNotification.getNotification();

        // Field for retrieving Phone No in a guaranteed manner
        // Format is, 918368978651@s.whatsapp.net
        String tag = statusBarNotification.getTag();

        Bundle extras = notification.extras;
        //Util.listAllNotificationExtraKeysAndValues(extras);
        // Could be a Name or a Number - If the sender's WhatsApp contact has a name,
        // we would get a name here. Otherwise, we would get a number.
        String title = extras.getString(Notification.EXTRA_TITLE);

        // Before doing any processing on the Title, there is an edge case where the notification
        // received is from the company, WhatsApp. Ignore in that case.
        if(title == null || title.equals("WhatsApp") || tag == null){
            return null;
        }

        // Message to be built
        WhatsAppMessage message = new WhatsAppMessage();

        String phoneNo = tag.replace("@s.whatsapp.net", "");

        // Either the title is a Name or a Phone No(in case of new contact). In
        // both the cases, we store it as a Name.
        message.setSenderName(title);

        String text = extras.getString(Notification.EXTRA_TEXT);
        // In case of multiple messages at once or multiple unread, it will be something like,
        // "X new messages" or content of a single message in case of single one. The assumption
        // is that Tag will be null in this case. So, we are safe with direct setting of text.
        message.setMessageText(text);

        message.setPhoneNo(phoneNo);

        // Time is easy!
        message.setMessageTime(notification.when);

        return message;
    }

    private Contact buildContact(WhatsAppMessage message){
        Contact contact = new Contact(message.getPhoneNo(), message.getSenderName());

        return contact;
    }
}
