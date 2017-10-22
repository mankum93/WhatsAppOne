package com.whatsappone.whatsappone.database;

/**
 * Created by DJ on 10/15/2017.
 */

public class ContactSchema {

    public static final class ContactTable{

        public static final String NAME = "contact_info";

        public static final class cols{

            public static final String CONTACT_PHONE_NO = "phoneNo";

            public static final String CONTACT_NAME = "contactName";
        }
    }

    public static final class MessagesRecordsTable {

        public static final String NAME = "messages_records_table";

        public static final class cols{
            // Foreign Key
            public static final String MESSAGE_SENDER_PHONE_NO = "messageSenderPhoneNo";
            public static final String MESSAGE_SENDER_NAME = "messageSenderName";
            public static final String MESSAGE_BODY = "messageBody";
            public static final String MESSAGE_TIMESTAMP = "messageTimestamp";
            public static final String MESSAGE_READ_STATUS = "messageReadStatus";
        }
    }
}
