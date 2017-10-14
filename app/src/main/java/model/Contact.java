package model;

/**
 * Created by DJ on 10/15/2017.
 */

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Class modeling a Contact.
 */
public class Contact implements Parcelable {

    /**
     * Phone No of Contact.
     */
    private String contactPhoneNo;

    /**
     * Name of the Contact.
     */
    private String contactName;

    /**
     * Both Phone No and Name are essential for being a valid contact
     * @param contactPhoneNo Phone No
     * @param contactName Contact Name.
     */
    public Contact(@NonNull String contactPhoneNo, @NonNull String contactName) {
        this.contactPhoneNo = contactPhoneNo;
        this.contactName = contactName;
    }

    public String getContactPhoneNo() {
        return contactPhoneNo;
    }

    public String getContactName() {
        return contactName;
    }

    protected Contact(Parcel in) {
        contactPhoneNo = in.readString();
        contactName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contactPhoneNo);
        dest.writeString(contactName);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
