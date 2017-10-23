package com.whatsappone.whatsappone.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.Pair;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.Contact;
import model.WhatsAppMessage;

/**
 * Created by DJ on 5/17/2017.
 */

public class ContactsDbHelper extends SQLiteOpenHelper{

    private static final String TAG = "ContactsDbHelper";

    private static final int VERSION  = 1;
    public static final String DATABASE_NAME = "contact_record.db";

    public ContactsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createContactTables(db);
        createMessageRecordsTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void createContactTables(SQLiteDatabase db){

        db.execSQL("CREATE TABLE " + ContactSchema.ContactTable.NAME + "(" +
                ContactSchema.ContactTable.cols.CONTACT_PHONE_NO + " NOT NULL " + " PRIMARY KEY " + "," +
                ContactSchema.ContactTable.cols.CONTACT_NAME + " NOT NULL " + ")"
        );
        Log.d(TAG, "Contact table creation complete");
    }

    public static void createMessageRecordsTable(SQLiteDatabase db){
        if(!isExistingTable(db, ContactSchema.MessagesRecordsTable.NAME)){
            db.execSQL("CREATE TABLE " + ContactSchema.MessagesRecordsTable.NAME + "(" +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " NOT NULL " + "," +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME + " NOT NULL " + "," +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_BODY + "," +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " NOT NULL " + "," +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS + " NOT NULL " + "," +
                    " PRIMARY KEY (" + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + "," +
                    ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + ")" + ")"
            );
            Log.d(TAG, "Contacts Messages Records table creation complete.");
        }
    }


    // CONTENT VALUES---------------------------------------------------------------------------------------------------

    public static ContentValues getContentValues(Contact contact){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactSchema.ContactTable.cols.CONTACT_PHONE_NO, contact.getContactPhoneNo());
        contentValues.put(ContactSchema.ContactTable.cols.CONTACT_NAME, contact.getContactName());

        return contentValues;
    }

    public static ContentValues getContentValues(WhatsAppMessage message){
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO, message.getPhoneNo());
        contentValues.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME, message.getSenderName());
        contentValues.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_BODY, message.getMessageText());
        contentValues.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP, message.getMessageTimeMillis());
        contentValues.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS, message.getMessageReadStatus() ? 1 : 0);

        return contentValues;
    }

    // DB OPERATIONS---------------------------------------------------------------------------------------------------

    public static void insertContactToDb(SQLiteDatabase db, Contact contact){

        db.insertWithOnConflict(ContactSchema.ContactTable.NAME, null, getContentValues(contact), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void insertContactsToDb(SQLiteDatabase db, Contact[] contacts){

        for(Contact contact : contacts){
            db.insertWithOnConflict(ContactSchema.ContactTable.NAME, null, getContentValues(contact), SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public static void insertContactsToDb(SQLiteDatabase db, List<Contact> contacts){

        for(Contact contact : contacts){
            db.insertWithOnConflict(ContactSchema.ContactTable.NAME, null, getContentValues(contact), SQLiteDatabase.CONFLICT_IGNORE);
        }
    }

    public static void updateContactInDb(SQLiteDatabase database, Contact contact){

        ContentValues values = getContentValues(contact);
        database.update(ContactSchema.ContactTable.NAME, values,
                ContactSchema.ContactTable.cols.CONTACT_PHONE_NO + " = ? ", new String[]{contact.getContactPhoneNo()});
    }

    public static void updateMessagesTableWithNewNameConditionally(SQLiteDatabase database, String phoneNo, String newName){
        ContentValues values = new ContentValues();
        values.put(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME, newName);
        database.update(ContactSchema.MessagesRecordsTable.NAME, values, " WHERE " +
                ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? ", new String[]{phoneNo});
    }

    //--------------------------------------------------------------------------------------------------------------------------

    public static long insertMessageToDb(SQLiteDatabase db, WhatsAppMessage message){
        return db.insertWithOnConflict(ContactSchema.MessagesRecordsTable.NAME, null, getContentValues(message), SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static void insertOrUpdateMessageToDb(SQLiteDatabase db, WhatsAppMessage message){
        if(isMessagePresentInDb(db, message)){
            // Update
            updateMessageToDb(db, message);
        }
        else{
            // Insert
            insertMessageToDb(db, message);
        }
    }

    /*public static long insertOrUpdateMessageToDb(SQLiteDatabase db, WhatsAppMessage message){
        db.execSQL("INSERT OR REPLACE INTO " + ContactSchema.MessagesRecordsTable.NAME + " ( " +
                ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " , " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " , " +
                ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS + " ) " +
        " VALUES " + " ( ? , ?, ? ) ", new String[]{message.getPhoneNo(), String.valueOf(message.getMessageTimeStamp()),
                String.valueOf(message.getMessageReadStatus() ? 1 : 0)});
        return db.insertWithOnConflict(ContactSchema.MessagesRecordsTable.NAME, null, getContentValues(message), SQLiteDatabase.CONFLICT_IGNORE);
    }*/

    public static void updateMessageToDb(SQLiteDatabase database, WhatsAppMessage message){
        ContentValues values = getContentValues(message);

        database.update(ContactSchema.MessagesRecordsTable.NAME, values,
                ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? ", new String[]{message.getPhoneNo()});
    }

    public static int removeMessageFromDb(SQLiteDatabase db, WhatsAppMessage message){
        return db.delete(ContactSchema.MessagesRecordsTable.NAME, ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? " +
                " AND " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " = CAST(? AS NUMBER) ", new String[]{message.getPhoneNo(), String.valueOf(message.getMessageTimeMillis())});
    }

    public static void removeMessageFromDb2(SQLiteDatabase db, WhatsAppMessage message){
        db.execSQL("DELETE FROM " + ContactSchema.MessagesRecordsTable.NAME +
                " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? " +
                " AND " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " = ? ", new Object[]{message.getPhoneNo(), message.getMessageTimeMillis()});
    }

    public static int removeMessagesFromDb(SQLiteDatabase db, List<WhatsAppMessage> messages){
        int affectedRowCount = 0;
        for(int i = 0; i < messages.size(); i++){
            affectedRowCount += db.delete(ContactSchema.MessagesRecordsTable.NAME, ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? " +
                    " AND " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " = ? ", new String[]{messages.get(i).getPhoneNo(), String.valueOf(messages.get(i).getMessageTimeMillis())});
        }
        return affectedRowCount;
    }

    //----------------------------------------------------------------------------------------------

    public static boolean isExistingTable(SQLiteDatabase db, String tableName){

        Cursor c = null;
        try{
            c = db.rawQuery("SELECT name FROM " + "sqlite_master" + " WHERE type = 'table' AND name = ? ",
                    new String[]{tableName});
        }
        finally {
            if(c != null){
                if(c.getCount() == 0){
                    c.close();
                    return false;
                }
                c.close();
                return true;
            }
        }
        return false;
    }

    public static Contact isContactPresentInDb(SQLiteDatabase db, String phoneNo){

        Cursor c = null;
        try{
            c = db.rawQuery("SELECT * FROM " + ContactSchema.ContactTable.NAME +
                    " WHERE " + ContactSchema.ContactTable.cols.CONTACT_PHONE_NO +
                    " = ? ", new String[]{phoneNo});

            if(c.moveToFirst()){
                Contact contact = new Contact(c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_PHONE_NO)),
                        c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_NAME)));

                return contact;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }


        return null;
    }

    public static boolean isMessagePresentInDb(SQLiteDatabase db, WhatsAppMessage message){
        Cursor c = null;
        try{
            c = db.rawQuery("SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME +
                    " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? " +
                    " AND " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " = CAST(? AS NUMBER) ",
                    new String[]{message.getPhoneNo(), String.valueOf(message.getMessageTimeMillis())});

            if(c.getCount() > 0){
                return true;
            }
        }finally {
            if(c != null){
                c.close();
            }
        }


        return false;
    }

    // READ------------------------------------------------------------------------------------------------------------

    public static final int SORT_ORDER_ASC = 0x002;
    public static final int SORT_ORDER_DESC = 0x004;
    public static final int SORT_ORDER_NONE = 0x008;
    public static final int SORT_BY_MSG_TIMESTAMPS = 0x0016;
    public static List<WhatsAppMessage> getAllMessageRecordsFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        List<WhatsAppMessage> messages;

        Cursor cMsg = null;

        try{
            switch(sortOrder){

                case SORT_ORDER_ASC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " ORDER BY " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " ASC "
                            , null);
                    break;

                case SORT_ORDER_DESC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " ORDER BY " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " DESC "
                            , null);
                    break;

                case SORT_ORDER_NONE:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                            , null);
                    break;

                default:
                    // No such(the provided one) order valid.
                    Log.w(TAG, "The provided sort order: " + sortOrder + " is invalid.");
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                            , null);
                    break;
            }

            if(!cMsg.moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "No message record found.");
                return null;
            }
            else{
                messages = new ArrayList<>(cMsg.getCount());

                do{
                    WhatsAppMessage message = new WhatsAppMessage(cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME)),
                            cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO)),
                            cMsg.getLong(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP)),
                            cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_BODY)),
                            cMsg.getInt(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS)) == 1);

                    // Add Message to the list
                    messages.add(message);

                }while(cMsg.moveToNext());
            }
        }
        finally {
            if(cMsg != null){
                cMsg.close();
            }
        }

        return messages;

    }

    public static List<WhatsAppMessage> getAMessagesRecordFromDb(SQLiteDatabase db, String contactPhoneNo, int sortOrder){

        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }
        List<WhatsAppMessage> messages;

        Cursor cMsg = null;

        try{
            switch(sortOrder){

                case SORT_ORDER_ASC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? "
                                    + " ORDER BY " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " ASC "
                            , new String[]{contactPhoneNo});
                    break;

                case SORT_ORDER_DESC:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? "
                                    + " ORDER BY " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " DESC "
                            , new String[]{contactPhoneNo});
                    break;

                case SORT_ORDER_NONE:
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? "
                            , new String[]{contactPhoneNo});
                    break;

                default:
                    // No such(the provided one) order valid.
                    Log.w(TAG, "The provided sort order: " + sortOrder + " is invalid.");
                    cMsg = db.rawQuery(
                            "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                                    + " WHERE " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO + " = ? "
                            , new String[]{contactPhoneNo});
                    break;
            }

            if(!cMsg.moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "No message record found.");
                return null;
            }
            else{
                messages = new ArrayList<>(cMsg.getCount());

                do{
                    WhatsAppMessage message = new WhatsAppMessage(cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME)),
                            cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO)),
                            cMsg.getLong(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP)),
                            cMsg.getString(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_BODY)),
                            cMsg.getInt(cMsg.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS)) == 1);

                    // Add Message to the list
                    messages.add(message);

                }while(cMsg.moveToNext());
            }
        }
        finally {
            if(cMsg != null){
                cMsg.close();
            }
        }

        return messages;

    }

    public static Map<Contact, List<WhatsAppMessage>> getAllContactsAndMessagesFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }

        // Correct the sort order if its not.
        switch(sortOrder){
            case SORT_ORDER_ASC:
                break;
            case SORT_ORDER_DESC:
                break;
            case SORT_ORDER_NONE:
                break;
            default:
                sortOrder = SORT_ORDER_NONE;
                break;
        }

        Map<Contact, List<WhatsAppMessage>> contactsAndMessages;
        Cursor c = null, cAddr = null;
        // Before running the query, check if the Table exists
        if(!isExistingTable(db, ContactSchema.ContactTable.NAME)){
            return null;
        }

        try{
            c = db.rawQuery("SELECT * FROM " + ContactSchema.ContactTable.NAME, null);
            contactsAndMessages = new LinkedHashMap<Contact, List<WhatsAppMessage>>(c.getCount());

            if(!c .moveToFirst()){
                // Data is corrupt.
                Log.e(TAG, "Contact data is corrupt.(Names)");
                return null;
            }
            else{
                do{

                    List<WhatsAppMessage> messages;

                    Contact contact;

                    // Get the Phone No. first
                    String phoneNo = c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_PHONE_NO));

                    contact = new Contact(c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_NAME)), phoneNo);

                    // Retrieve the list of all the messages sent to this contact.
                    messages = getAMessagesRecordFromDb(db, phoneNo, sortOrder);

                    // Update the Map
                    contactsAndMessages.put(contact, messages);

                }while(c.moveToNext());
            }
        }
        finally {
            if(c != null){
                c.close();
            }
        }


        return contactsAndMessages;
    }

    /**
     * Get the entire messages' combined sent history sorted by the specified order with
     * A {@link Map} of all the {@link Contact}(s) for which at least one message has been sent.
     *
     * @param db : A readable {@link SQLiteDatabase} instance.
     * @param sortOrder : The order in which the entire(combined) message history should be sorted
     * @return : Mapping of every {@link WhatsAppMessage} sent and its corresponding {@link Contact}
     */
    public static List<Pair<WhatsAppMessage, Contact>> getAllMessagesAndContactsFromDb(SQLiteDatabase db, int sortOrder){
        if(!db.isOpen()){
            throw new SQLiteDatabaseLockedException("Database not open for reading");
        }

        String sOrder = null;
        // Correct the sort order if its not.
        switch(sortOrder){
            case SORT_ORDER_ASC:
                sOrder = "ASC";
                break;
            case SORT_ORDER_DESC:
                sOrder = "DESC";
                break;
            case SORT_ORDER_NONE:
                break;
            default:
                sortOrder = SORT_ORDER_NONE;
                break;
        }

        Map<String, Contact> contactsMap = new HashMap<>();
        List<Pair<WhatsAppMessage, Contact>> messagesAndContacts;

        Cursor c = null, cAddr = null;
        // Before running the query, check if the Tables exist
        if(!isExistingTable(db, ContactSchema.ContactTable.NAME)){
            Log.e(TAG, "Contact Table doesn't exist.");
            return null;
        }
        if(!isExistingTable(db, ContactSchema.MessagesRecordsTable.NAME)){
            Log.e(TAG, "Messages Table doesn't exist.");
            return null;
        }

        try{
            // Create a JOIN query for the Contacts and Messages Table.
            c = db.rawQuery(
                    "SELECT * FROM " + ContactSchema.MessagesRecordsTable.NAME
                            + " LEFT JOIN " + ContactSchema.ContactTable.NAME
                            + " ON " + ContactSchema.ContactTable.cols.CONTACT_PHONE_NO + " = " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO
                            + (sOrder != null ? (" ORDER BY " + ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP + " " + sOrder) : "")
                    , null);

            messagesAndContacts = new LinkedList<>();

            if(!c .moveToFirst()){
                Log.e(TAG, "Both Contact and Messages tables have no data.");
                return null;
            }
            else{

                do{

                    Contact contact;
                    WhatsAppMessage message = null;

                    // Get the Phone No. first
                    String phoneNo = c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_PHONE_NO));
                    // Check if this Contact for this Phone No has already been retrieved.
                    if(contactsMap.get(phoneNo) != null){
                        // Reuse the same contact
                        contact = contactsMap.get(phoneNo);
                    }
                    else{
                        // Build a new contact.

                        contact = new Contact(phoneNo, c.getString(c.getColumnIndex(ContactSchema.ContactTable.cols.CONTACT_NAME)));

                        // Save this contact for reuse.
                        contactsMap.put(phoneNo, contact);
                    }

                    // Create a Message
                    message = new WhatsAppMessage(c.getString(c.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_NAME)),
                            c.getString(c.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_SENDER_PHONE_NO)),
                            c.getLong(c.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_TIMESTAMP)),
                            c.getString(c.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_BODY)),
                            c.getInt(c.getColumnIndex(ContactSchema.MessagesRecordsTable.cols.MESSAGE_READ_STATUS)) == 1);

                    // Pair up this message with the contact
                    messagesAndContacts.add(new Pair<WhatsAppMessage, Contact>(message, contact));

                }while(c.moveToNext());
            }
        }
        finally {
            if(c != null){
                c.close();
            }
        }

        return messagesAndContacts;
    }

    // Utility Methods------------------------------------------------------------------------------------------------------
    public static File isExistingDatabase(Context context, String databaseName) {

        File dbPath = context.getDatabasePath(databaseName);
        return dbPath;
    }
}
