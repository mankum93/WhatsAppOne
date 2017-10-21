package com.whatsappone.whatsappone.services;

import android.app.Notification;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.whatsappone.whatsappone.WhatsAppOneApplication;
import com.whatsappone.whatsappone.database.ContactsDbHelper;
import com.whatsappone.whatsappone.util.Util;

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

    private static final String TAG = "WhatsApNotifListService";

    private SQLiteDatabase db;

    @Override
    public void onCreate() {
        super.onCreate();

        // Get the Single Db instance
        db = WhatsAppOneApplication.dbInstance;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        String packageName = sbn.getPackageName();

        // Only the WhatsApp messages are relevant
        if(packageName.contains("com.whatsapp")){

            // DEBUG: Dump the notification
            //if(BuildConfig.DEBUG){
                Log.d(TAG, sbn.toString());
                Log.d(TAG, "--------------------------------------------------");
                Log.d(TAG, "when" + sbn.getNotification().when);
                Log.d(TAG, "--------------------------------------------------");
            //}

            WhatsAppMessage message = buildMessage(sbn);

            if(message != null){

                // Build a Contact
                Contact contact = buildContact(message);

                // Check if there is a Name with the Message
                String str = message.getSenderName().replaceAll("[^0-9]","");
                if(!str.equals(message.getPhoneNo())){
                    // The name could have been deliberately set to a number but
                    // wouldn't be a problem for us

                    // Check if the Phone No is in Db for the Contact
                    Contact cont = ContactsDbHelper.isContactPresentInDb(db, message.getPhoneNo());
                    if(cont != null){
                        // Does this Contact have a proper name?
                        if(!cont.getContactName().equals(message.getSenderName())){
                            // There is a new valid Name; update the Contacts table
                            ContactsDbHelper.updateContactInDb(db, contact);
                            // Also, update the Messages table to replace the Name with this
                            // new/proper one instead of a number
                            ContactsDbHelper.updateMessagesTableWithNewNameConditionally(db, message.getPhoneNo(), message.getSenderName());
                            // TODO: Update the Cache(current UI) to reflect these changes
                        }

                    }
                    else{
                        // New Contact
                        ContactsDbHelper.insertContactToDb(db, contact);
                    }

                }
                else{
                    // In case they are same, it's possible that the Contact was
                    // changed in the following ways:
                    // 1) User may have lost it
                    // 2) User may have deliberately set the Name to Number(practically highly unlikely)
                    //
                    // Check the Db for an existing Contact and a valid
                    // Name if there is one. In that case, set that Name for this message
                    // before writing it to Db and displaying.
                    Contact cont = ContactsDbHelper.isContactPresentInDb(db, message.getPhoneNo());
                    if(cont != null){
                        if(!cont.getContactPhoneNo().equals(cont.getContactName())){
                            // Existing Contact. Valid Name.
                            message.setSenderName(cont.getContactName());
                        }
                    }
                }

                // Write it to the Database and update the UI.

                // Database

                // If the message is an identical one(literally w.r.t to everything),
                // check if it gets inserted in the Db successfully. If not, don't
                // update the UI either.
                if((ContactsDbHelper.insertMessageToDb(db, message)) == -1 ){
                    // Error or CONFLICT_IGNORE
                    return;
                }

                // Send a Broadcast; Update the UI
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

        // DEBUG: Dump the Notification extra {key, value} pairs.
        //if(BuildConfig.DEBUG){
            Util.listAllNotificationExtraKeysAndValues(extras);
        //}

        // Could be a Name or a Number - If the sender's WhatsApp contact has a name,
        // we would get a name here. Otherwise, we would get a number.
        String title = extras.getString(Notification.EXTRA_TITLE);

        // Before doing any processing on the Title, there is an edge case where the notification
        // received is from the company, WhatsApp. Ignore in that case.
        if(tag == null || title == null || title.equals("WhatsApp")){
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
