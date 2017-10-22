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
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_FROM;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_INSERT_MESSAGE;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_NEW_MESSAGE;
import static com.whatsappone.whatsappone.services.ChatHeadsService.EXTRA_UPDATE_NAME;

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
public class WhatsAppNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "WhatsApNotifListService";

    public static final String FROM = "WhatsAppNotificationListenerService";

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

                // Check if this is a new Contact
                Contact cont = ContactsDbHelper.isContactPresentInDb(db, message.getPhoneNo());

                Intent intent = null;

                if(cont == null){
                    // Insert the Contact as well the message afresh
                    Contact contact = buildContact(message);
                    ContactsDbHelper.insertContactToDb(db, contact);
                    // TODO: Ask the receiver to insert the message afresh
                    intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                    intent.putExtra(EXTRA_FROM, FROM);
                    intent.putExtra(EXTRA_NEW_MESSAGE, message);
                    intent.putExtra(EXTRA_UPDATE_NAME, false);
                    intent.putExtra(EXTRA_INSERT_MESSAGE, true);
                }
                else{

                    boolean isNameAvailable = false;

                    // Check if there is a new Name with the Message
                    if(message.getSenderName().equals(message.getPhoneNo())){
                        // No, there isn't. In that case, do we have a valid name from
                        // before?
                        if(!cont.getContactName().equals(cont.getContactPhoneNo())){
                            // Yes, modify the current message to incorporate the same
                            message.setSenderName(cont.getContactName());
                        }

                        // Do we have a message duplicate?
                        if(!ContactsDbHelper.isMessagePresentInDb(db, message)){
                            intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                            intent.putExtra(EXTRA_FROM, FROM);
                            intent.putExtra(EXTRA_NEW_MESSAGE, message);
                            intent.putExtra(EXTRA_INSERT_MESSAGE, true);
                            // Insert this message afresh.
                            // TODO: Ask the receiver to do the same
                        }
                        else{
                            return;
                        }
                    }
                    else{
                        // There is a name with the message
                        // Is it the same name from before?
                        if(!message.getSenderName().equals(cont.getContactName())){
                            isNameAvailable = true;
                        }

                        // Do we have a message duplicate?
                        if(ContactsDbHelper.isMessagePresentInDb(db, message)){
                            if(isNameAvailable){
                                intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                                intent.putExtra(EXTRA_FROM, FROM);
                                intent.putExtra(EXTRA_UPDATE_NAME, true);
                                intent.putExtra(EXTRA_NEW_MESSAGE, message);
                                intent.putExtra(EXTRA_INSERT_MESSAGE, false);
                                // At least, we have name to update. Just update
                                // the name for all the previous messages
                                // TODO: Ask the receiver to do the same
                            }
                            else{
                                return;
                            }
                        }
                        else{
                            if(isNameAvailable){
                                // First, update the Name of previous messages
                                // Then, insert this message afresh.
                                intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                                intent.putExtra(EXTRA_FROM, FROM);
                                intent.putExtra(EXTRA_NEW_MESSAGE, message);
                                intent.putExtra(EXTRA_UPDATE_NAME, true);
                                intent.putExtra(EXTRA_INSERT_MESSAGE, true);
                                // TODO: Ask the receiver to do the same
                            }
                            else{
                                intent = new Intent(getApplicationContext(), ChatHeadsService.class);
                                intent.putExtra(EXTRA_FROM, FROM);
                                intent.putExtra(EXTRA_NEW_MESSAGE, message);
                                intent.putExtra(EXTRA_INSERT_MESSAGE, true);
                                // Just insert this message as it is afresh
                                // TODO: Ask the receiver to do the same
                            }
                        }

                        if(isNameAvailable){
                            // Update the Contact with new Name
                            // Build the Contact
                            Contact contact = buildContact(message);
                            ContactsDbHelper.updateContactInDb(db, contact);
                        }

                    }
                }

                // Start the ChatHeads service if not running already
                // or send a new Intent if it is
                startService(intent);
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
        // both the cases, we want to store it as a Name.
        // Okay, currently, if WhatsApp sends out a Number as a Name(title)(when we don't
        // have the person as a WhatsApp contact), it is sent in the following format:
        // 91-9818279... or +91 92392323. We try to extract the proper number from title
        // if there is one
        String temp = title.replaceAll("[- +\\u202A\\u202C]", "");
        if(!temp.equals(phoneNo)){
            // It is a Name
            message.setSenderName(title);
        }
        else{
            // It is a Number
            message.setSenderName(phoneNo);
        }


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
