package com.whatsappone.whatsappone.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DJ on 5/17/2017.
 */

public class DateTimeUtils {

    //Formatting the timestamp for human readability
    public static String timeMillisToHH_MMFormat(long timeStampInMilliSec){
        Date d = new Date(timeStampInMilliSec);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(d);
    }

    //Formatting the timestamp for human readability
    public static String timeMillisToHH_MMFormat(long timeStampInMilliSec, String format){
        Date d = new Date(timeStampInMilliSec);
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(d);
    }

    //Formatting the timestamp for human readability
    public static String timeStampToHH_MMFormat(Timestamp timestamp){
        long timeStampInMilliSec = timestamp.getTime();
        Date d = new Date(timeStampInMilliSec);
        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        return dateFormat.format(d);
    }
}
