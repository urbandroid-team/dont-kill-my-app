package com.evenwell.powersaving.g3.utils;

import android.util.Log;
import java.util.Calendar;
import java.util.Date;

public class TimeUtil {
    public static final int AMOUNT_OF_DEEP_DOZE_TIME = 2;
    private static final String TAG = "PowerSavingAppG3 TimeUtil";
    private static TimeUtil mInstance;
    private static final Calendar mTimeToApplyBreakTimeRestriction = Calendar.getInstance();
    private static final Calendar mTimeToApplyNightRestriction = Calendar.getInstance();
    private static final Calendar mTimeTounApplyBreakTimeRestriction = Calendar.getInstance();
    private static final Calendar mTimeTounApplyNightRestriction = Calendar.getInstance();

    public static TimeUtil getInstance() {
        if (mInstance == null) {
            mInstance = new TimeUtil();
        }
        return mInstance;
    }

    private TimeUtil() {
        initCalendarObj();
    }

    private void initCalendarObj() {
        mTimeToApplyBreakTimeRestriction.set(11, 18);
        mTimeToApplyBreakTimeRestriction.set(12, 0);
        mTimeToApplyBreakTimeRestriction.set(13, 0);
        mTimeToApplyNightRestriction.set(11, 1);
        mTimeToApplyNightRestriction.set(12, 0);
        mTimeToApplyNightRestriction.set(13, 0);
        mTimeTounApplyNightRestriction.set(11, 6);
        mTimeTounApplyNightRestriction.set(12, 0);
        mTimeTounApplyNightRestriction.set(13, 0);
        mTimeTounApplyBreakTimeRestriction.set(11, 8);
        mTimeTounApplyBreakTimeRestriction.set(12, 0);
        mTimeTounApplyBreakTimeRestriction.set(13, 0);
    }

    public boolean isInNightRestrictionTimeInteveral() {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int currentHour = now.get(11);
        Log.d(TAG, "currentHour : " + currentHour);
        if (currentHour < mTimeToApplyNightRestriction.get(11) || currentHour >= mTimeTounApplyNightRestriction.get(11)) {
            return false;
        }
        return true;
    }

    public boolean isInFirstBreakTimeRestrictionTimeInterval() {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int currentHour = now.get(11);
        Log.d(TAG, "currentHour : " + currentHour);
        if (currentHour >= mTimeToApplyBreakTimeRestriction.get(11) || currentHour < mTimeToApplyNightRestriction.get(11)) {
            return true;
        }
        return false;
    }

    public boolean isInSecondBreakTimeRestrictionTimeInterval() {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int currentHour = now.get(11);
        Log.d(TAG, "currentHour : " + currentHour);
        if (currentHour < mTimeTounApplyNightRestriction.get(11) || currentHour >= mTimeTounApplyBreakTimeRestriction.get(11)) {
            return false;
        }
        return true;
    }

    public boolean IsInRestrictionTimeInterval() {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int currentHour = now.get(11);
        Log.d(TAG, "currentHour : " + currentHour);
        if (currentHour >= mTimeToApplyBreakTimeRestriction.get(11) || currentHour < mTimeTounApplyBreakTimeRestriction.get(11)) {
            return true;
        }
        return false;
    }

    public String getCurrentInterval() {
        if (isInFirstBreakTimeRestrictionTimeInterval()) {
            return "FirstBreakTime";
        }
        if (isInNightRestrictionTimeInteveral()) {
            return "Night";
        }
        if (isInSecondBreakTimeRestrictionTimeInterval()) {
            return "SecondBreakTime";
        }
        return "WorkTime";
    }

    public boolean isInTimeInteveralToSendIntent() {
        Date date = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        int currentHour = now.get(11);
        Log.d(TAG, "currentHour : " + currentHour);
        if (currentHour >= mTimeToApplyBreakTimeRestriction.get(11) || currentHour <= mTimeTounApplyBreakTimeRestriction.get(11)) {
            return true;
        }
        return false;
    }
}
