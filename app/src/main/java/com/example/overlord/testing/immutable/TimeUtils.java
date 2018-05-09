package com.example.overlord.testing.immutable;

import java.util.Calendar;

/**
 * Created by overlord on 15/4/18.
 */

public class TimeUtils {
    public static int getSecondsFromTimestamp(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        return calendar.get(Calendar.SECOND);
    }

    public static long getTimeStamp() {
        return Calendar.getInstance().getTimeInMillis();
    }

}
