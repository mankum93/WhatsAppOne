package model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.sql.Timestamp;

/**
 * Created by DJ on 10/14/2017.
 */

/**
 * Class representing a WhatsApp message.
 */
public class WhatsAppMessage implements Parcelable, Comparable<WhatsAppMessage> {

    /**
     * Name of the message sender.
     */
    private String senderName;

    /**
     * Phone No of the Sender. Can serve as an Id to distinguish a set of messages.
     */
    private String phoneNo;

    /**
     * The textual content of the message.
     */
    private String messageText;

    /**
     * Timestamp for the message receive time.
     */
    private long messageTime;

    /**
     * Flag indicating if the message has been read or not.
     */
    private boolean messageReadStatus;


    /**
     * These 3 fields must hold validity(at least non null) for a valid construction.
     *
     * @param senderName Sender Name.
     * @param phoneNo Sender's Phone No.
     * @param messageTime The time of message.
     */
    public WhatsAppMessage(@NonNull String senderName, @NonNull String phoneNo, @NonNull long messageTime) {
        this.senderName = senderName;
        this.phoneNo = phoneNo;
        this.messageTime = messageTime;
    }

    public WhatsAppMessage(@NonNull String senderName, @NonNull String phoneNo, long messageTime, @NonNull String messageBody) {
        this.senderName = senderName;
        this.phoneNo = phoneNo;
        this.messageTime = messageTime;
        this.messageText = messageBody;
    }

    public WhatsAppMessage(@NonNull String senderName, @NonNull String phoneNo, long messageTime, @NonNull String messageBody, boolean messageReadStatus) {
        this.senderName = senderName;
        this.phoneNo = phoneNo;
        this.messageText = messageBody;
        this.messageTime = messageTime;
        this.messageReadStatus = messageReadStatus;
    }

    /**
     * Default constructor for constructing the object piece wise. Final object before being
     * committed to server or database must be checked for validity.
     */
    public WhatsAppMessage(){

    }

    public String getSenderName() {
        return senderName;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getMessageTimeMillis() {
        return messageTime;
    }

    public boolean getMessageReadStatus() {
        return messageReadStatus;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public void setMessageReadStatus(boolean messageReadStatus) {
        this.messageReadStatus = messageReadStatus;
    }

    // Not to be serialized/parcelabled
    public Timestamp getMessageTimeStamp() {
        return new Timestamp(messageTime);
    }

    // COMPARABLE IMPLEMENTATION--------------------------------------------------------------------

    /**
     * To help with sorting w.r.t timestamp.
     *
     * @param o The message to be compared with.
     * @return comparison.
     */
    @Override
    public int compareTo(@NonNull WhatsAppMessage o) {
        return this.getMessageTimeStamp().compareTo(o.getMessageTimeStamp());
    }


    // PARCELABLE IMPLEMENTATION--------------------------------------------------------------------

    protected WhatsAppMessage(Parcel in) {
        senderName = in.readString();
        phoneNo = in.readString();
        messageText = in.readString();
        messageTime = in.readLong();
        messageReadStatus = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(senderName);
        dest.writeString(phoneNo);
        dest.writeString(messageText);
        dest.writeLong(messageTime);
        dest.writeInt(messageReadStatus ? 1 : 0);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<WhatsAppMessage> CREATOR = new Parcelable.Creator<WhatsAppMessage>() {
        @Override
        public WhatsAppMessage createFromParcel(Parcel in) {
            return new WhatsAppMessage(in);
        }

        @Override
        public WhatsAppMessage[] newArray(int size) {
            return new WhatsAppMessage[size];
        }
    };
}
