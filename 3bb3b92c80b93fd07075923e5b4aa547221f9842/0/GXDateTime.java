//
// --------------------------------------------------------------------------
//  Gurux Ltd
// 
//
//
// Filename:        $HeadURL$
//
// Version:         $Revision$,
//                  $Date$
//                  $Author$
//
// Copyright (c) Gurux Ltd
//
//---------------------------------------------------------------------------
//
//  DESCRIPTION
//
// This file is a part of Gurux Device Framework.
//
// Gurux Device Framework is Open Source software; you can redistribute it
// and/or modify it under the terms of the GNU General Public License 
// as published by the Free Software Foundation; version 2 of the License.
// Gurux Device Framework is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of 
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
// See the GNU General Public License for more details.
//
// More information of Gurux products: http://www.gurux.org
//
// This code is licensed under the GNU General Public License v2. 
// Full text may be retrieved at http://www.gnu.org/licenses/gpl-2.0.txt
//---------------------------------------------------------------------------

package gurux.dlms;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gurux.dlms.enums.ClockStatus;
import gurux.dlms.enums.DateTimeSkips;
import gurux.dlms.internal.GXCommon;

public class GXDateTime {
    /**
     * Clock status.
     */
    private java.util.Set<ClockStatus> status;
    private Calendar meterCalendar;
    /**
     * Skipped fields.
     */
    private java.util.Set<DateTimeSkips> skip;
    /**
     * Daylight savings begin.
     */
    private boolean daylightSavingsBegin;
    /**
     * Daylight savings end.
     */
    private boolean daylightSavingsEnd;

    /**
     * Day of week.
     */
    private int dayOfWeek;

    /**
     * Constructor.
     */
    public GXDateTime() {
        skip = new HashSet<DateTimeSkips>();
        meterCalendar = Calendar.getInstance();
        status = new HashSet<ClockStatus>();
        status.add(ClockStatus.OK);
        dayOfWeek = 0;
    }

    /**
     * Constructor.
     * 
     * @param value
     *            Date value.
     */
    public GXDateTime(final Date value) {
        skip = new HashSet<DateTimeSkips>();
        meterCalendar = Calendar.getInstance();
        meterCalendar.setTime(value);
        status = new HashSet<ClockStatus>();
        status.add(ClockStatus.OK);
    }

    /**
     * Constructor.
     * 
     * @param value
     *            Date value.
     */
    public GXDateTime(final Calendar value) {
        skip = new HashSet<DateTimeSkips>();
        meterCalendar = value;
        status = new HashSet<ClockStatus>();
        status.add(ClockStatus.OK);
    }

    /**
     * Constructor.
     * 
     * @param year
     *            Used year.
     * @param month
     *            Used month.
     * @param day
     *            Used day.
     * @param hour
     *            Used hour.
     * @param minute
     *            Used minute.
     * @param second
     *            Used second.
     * @param millisecond
     *            Used millisecond.
     */
    public GXDateTime(final int year, final int month, final int day,
            final int hour, final int minute, final int second,
            final int millisecond) {
        meterCalendar = Calendar.getInstance();
        init(year, month, day, hour, minute, second, millisecond);
    }

    /**
     * Constructor.
     * 
     * @param year
     *            Used year.
     * @param month
     *            Used month.
     * @param day
     *            Used day.
     * @param hour
     *            Used hour.
     * @param minute
     *            Used minute.
     * @param second
     *            Used second.
     * @param millisecond
     *            Used millisecond.
     * @param timeZone
     *            Used time Zone.
     * @deprecated use {@link #GXDateTime} instead.
     */
    public GXDateTime(final int year, final int month, final int day,
            final int hour, final int minute, final int second,
            final int millisecond, final int timeZone) {
        meterCalendar = Calendar.getInstance(getTimeZone(timeZone, true));
        init(year, month, day, hour, minute, second, millisecond);
    }

    /**
     * Initialize settings.
     * 
     * @param year
     *            Used year.
     * @param month
     *            Used month.
     * @param day
     *            Used day.
     * @param hour
     *            Used hour.
     * @param minute
     *            Used minute.
     * @param second
     *            Used second.
     * @param millisecond
     *            Used millisecond.
     */
    protected void init(final int year, final int month, final int day,
            final int hour, final int minute, final int second,
            final int millisecond) {
        int y = year;
        int m = month;
        int d = day;
        int h = hour;
        int min = minute;
        int s = second;
        int ms = millisecond;
        skip = new HashSet<DateTimeSkips>();
        status = new HashSet<ClockStatus>();
        status.add(ClockStatus.OK);
        if (y < 1 || y == 0xFFFF) {
            skip.add(DateTimeSkips.YEAR);
            Calendar tm = Calendar.getInstance();
            y = tm.get(Calendar.YEAR);
        }
        daylightSavingsBegin = m == 0xFE;
        daylightSavingsEnd = m == 0xFD;
        if (m < 1 || m > 12) {
            skip.add(DateTimeSkips.MONTH);
            m = 0;
        } else {
            m -= 1;
        }

        if (d == -1 || d == 0 || d > 31) {
            skip.add(DateTimeSkips.DAY);
            d = 1;
        } else if (d < 0) {
            Calendar cal = Calendar.getInstance();
            d = cal.getActualMaximum(Calendar.DATE) + d + 3;
        }
        if (h < 0 || h > 24) {
            skip.add(DateTimeSkips.HOUR);
            h = 0;
        }
        if (min < 0 || min > 60) {
            skip.add(DateTimeSkips.MINUTE);
            min = 0;
        }
        if (s < 0 || s > 60) {
            skip.add(DateTimeSkips.SECOND);
            s = 0;
        }
        // If ms is Zero it's skipped.
        if (ms < 1 || ms > 1000) {
            skip.add(DateTimeSkips.MILLISECOND);
            ms = 0;
        }
        meterCalendar.set(y, m, d, h, min, s);
        if (ms != 0) {
            meterCalendar.set(Calendar.MILLISECOND, ms);
        }
    }

    /**
     * Constructor
     * 
     * @param value
     *            Date time value as a string.
     */
    public GXDateTime(final String value) {
        if (value != null) {
            int year = 2000, month = 1, day = 1, hour = 0, min = 0, sec = 0;
            SimpleDateFormat sd = new SimpleDateFormat();
            // Separate date and time parts.
            List<String> tmp = GXCommon.split(sd.toPattern(), " ");
            List<String> shortDatePattern = new ArrayList<String>();
            List<String> shortTimePattern = new ArrayList<String>();
            char dateSeparator = 0;
            char timeSeparator = 0;
            int dateIndex = 0;
            int timeIndex = 1;

            if (tmp.get(0).indexOf('H') != -1) {
                timeIndex = 0;
                dateIndex = 1;
            } else if (tmp.get(1).indexOf('a') != -1) {
                timeIndex = 2;
                if (tmp.size() == 2) {
                    String str = tmp.get(1);
                    tmp.remove(1);
                    int pos = str.indexOf('a');
                    if (pos == 0) {
                        tmp.add("a");
                        tmp.add(str.substring(1));
                    } else {
                        timeIndex = 1;
                        tmp.add(str.substring(0, pos));
                        tmp.add("a");
                    }
                }
            }

            if (Locale.getDefault() == Locale.KOREAN
                    || Locale.getDefault() == Locale.KOREA) {
                dateSeparator = ' ';
                timeSeparator = ':';
                tmp.clear();
                tmp.add("yy. M. d");
                tmp.add("a");
                tmp.add("h:mm");
            } else {
                // Find date separator.
                for (char it : tmp.get(dateIndex).toCharArray()) {
                    if (!Character.isLetter(it)) {
                        dateSeparator = it;
                        break;
                    }
                }
                // Find time separator.
                for (char it : tmp.get(timeIndex).toCharArray()) {
                    if (!Character.isLetter(it)) {
                        timeSeparator = it;
                        break;
                    }
                }
                shortDatePattern.addAll(
                        GXCommon.split(tmp.get(dateIndex), dateSeparator));
            }
            int offset = 3;
            if ("a".compareToIgnoreCase(tmp.get(1)) == 0) {
                shortTimePattern
                        .addAll(GXCommon.split(tmp.get(2), timeSeparator));
                offset = 4;
            } else {
                if (timeIndex == 0) {
                    shortTimePattern
                            .addAll(GXCommon.split(tmp.get(0), timeSeparator));
                } else {
                    shortTimePattern
                            .addAll(GXCommon.split(tmp.get(1), timeSeparator));
                }
            }
            // Add seconds if not used.
            if (!shortTimePattern.contains("ss")) {
                if (shortTimePattern.get(shortTimePattern.size() - 1)
                        .equals("a")) {
                    shortTimePattern.remove("a");
                    shortTimePattern.add("ss");
                    shortTimePattern.add("a");
                } else {
                    shortTimePattern.add("ss");
                }
            }
            List<String> values = GXCommon.split(value.trim(),
                    new char[] { dateSeparator, timeSeparator, ' ' });

            for (int pos = 0; pos != shortDatePattern.size(); ++pos) {
                boolean ignore = false;
                if ("*".equals(values.get(pos))) {
                    ignore = true;
                }
                String val = shortDatePattern.get(pos);
                if (val.startsWith("y")) {
                    if (ignore) {
                        year = -1;
                    } else {
                        year = Integer.parseInt(values.get(pos));
                        if (val.compareToIgnoreCase("yy") == 0) {
                            year += 2000;
                        }
                    }
                } else if ("M".compareToIgnoreCase(val) == 0
                        || "MM".compareToIgnoreCase(val) == 0
                        || "M.".compareToIgnoreCase(val) == 0) {
                    if (ignore) {
                        month = -1;
                    } else {
                        month = Integer.parseInt(values.get(pos));
                    }
                } else if ("d".compareToIgnoreCase(val) == 0
                        || "dd".compareToIgnoreCase(val) == 0) {
                    if (ignore) {
                        day = -1;
                    } else {
                        day = Integer.parseInt(values.get(pos));
                    }
                } else if ("".compareToIgnoreCase(val) == 0) {
                    // Skip this.
                } else if ("Gy".compareToIgnoreCase(val) == 0) {
                    // Skip this.
                } else {
                    throw new IllegalArgumentException(
                            "Invalid Date time pattern.");
                }
            }
            if (values.size() > 3) {
                SimpleDateFormat s = new SimpleDateFormat("a");
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(0));
                String am = s.format(new Date(0));
                cal.add(Calendar.HOUR_OF_DAY, 12);
                String pm = s.format(cal.getTime());

                for (int pos = 0; pos != values.size() - offset; ++pos) {
                    boolean ignore = false;
                    if ("*".equals(values.get(offset + pos))) {
                        ignore = true;
                    }
                    String val = shortTimePattern.get(pos);
                    if ("h".compareToIgnoreCase(val) == 0
                            || "hh".compareToIgnoreCase(val) == 0) {
                        if (ignore) {
                            hour = -1;
                        } else {
                            hour = Integer.parseInt(values.get(offset + pos));
                            // If AM/PM is before time.
                            if (offset == 4) {
                                if (pm.compareToIgnoreCase(
                                        values.get(3)) == 0) {
                                    hour += 12;
                                }
                            }
                        }
                    } else if ("mm".compareToIgnoreCase(val) == 0
                            || "M".compareToIgnoreCase(val) == 0) {
                        if (ignore) {
                            min = -1;
                        } else {
                            min = Integer.parseInt(values.get(offset + pos));
                        }
                    } else if ("ss".compareToIgnoreCase(val) == 0) {
                        val = values.get(offset + pos);
                        if (am.compareToIgnoreCase(val) == 0) {
                            if (hour == 12) {
                                hour = 0;
                            }
                        } else if (pm.compareToIgnoreCase(val) == 0) {
                            if (hour != 12) {
                                hour += 12;
                            }
                        } else {
                            if (ignore) {
                                sec = -1;
                            } else {
                                sec = Integer.parseInt(val);
                            }
                        }
                    } else {
                        throw new IllegalArgumentException(
                                "Invalid Date time pattern.");
                    }
                }
            }
            meterCalendar = Calendar.getInstance();
            init(year, month, day, hour, min, sec, 0);
        }

    }

    /**
     * @return Used calendar.
     * @deprecated use {@link #getMeterCalendar} instead.
     */
    public final Calendar getCalendar() {
        return meterCalendar;
    }

    /**
     * @return Used local calendar.
     */
    public final Calendar getLocalCalendar() {
        long meterTime = meterCalendar.getTime().getTime();
        Calendar local = Calendar.getInstance();
        int diff = meterCalendar.getTimeZone().getRawOffset()
                - local.getTimeZone().getRawOffset();
        long localtime = meterTime + diff;
        local.setTimeInMillis(localtime);
        // If meter is not use daylight saving time and client is.
        if (!meterCalendar.getTimeZone()
                .inDaylightTime(meterCalendar.getTime())) {
            if (local.getTimeZone().inDaylightTime(local.getTime())) {
                local.add(Calendar.HOUR_OF_DAY, -1);
            }
        } else {
            if (!local.getTimeZone().inDaylightTime(local.getTime())) {
                local.add(Calendar.HOUR_OF_DAY, 1);
            }
        }
        return local;
    }

    /**
     * @return Used meter calendar.
     */
    public final Calendar getMeterCalendar() {
        return meterCalendar;
    }

    /**
     * @param value
     *            Used meter calendar.
     */
    public final void setMeterCalendar(final Calendar value) {
        meterCalendar = value;
    }

    /**
     * @return Used date time value.
     * @deprecated use {@link #getLocalCalendar} instead.
     */
    @Deprecated
    public final java.util.Date getValue() {
        return meterCalendar.getTime();
    }

    /**
     * Set date time value.
     * 
     * @param forvalue
     *            Used date time value.
     * @deprecated use {@link #getLocalCalendar} instead.
     */
    @Deprecated
    public final void setValue(final java.util.Date forvalue) {
        meterCalendar.setTime(forvalue);
    }

    /**
     * Set date time value.
     * 
     * @param forvalue
     *            Used date time value.
     * @param forDeviation
     *            Used deviation.
     * @deprecated use {@link #setMeterCalendar} instead.
     */
    @Deprecated
    public final void setValue(final java.util.Date forvalue,
            final int forDeviation) {
        meterCalendar = Calendar.getInstance(getTimeZone(forDeviation, true));
        meterCalendar.setTime(forvalue);
    }

    /**
     * @return Skipped date time fields.
     */
    public final java.util.Set<DateTimeSkips> getSkip() {
        return skip;
    }

    /**
     * @param forValue
     *            Skipped date time fields.
     */
    public final void setSkip(final java.util.Set<DateTimeSkips> forValue) {
        skip = forValue;
    }

    public final void setUsed(final java.util.Set<DateTimeSkips> forValue) {
        int val = 0;
        for (DateTimeSkips it : forValue) {
            val |= it.getValue();
        }
        int tmp = (-1 & ~val);
        skip = DateTimeSkips.forValue(tmp);
    }

    /**
     * @return Day of week.
     */
    public final int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * @param forValue
     *            Day of week.
     */
    public final void setDayOfWeek(final int forValue) {
        dayOfWeek = forValue;
    }

    /**
     * @return Daylight savings begin.
     */
    public final boolean getDaylightSavingsBegin() {
        return daylightSavingsBegin;
    }

    /**
     * @param forValue
     *            Daylight savings begin.
     */
    public final void setDaylightSavingsBegin(final boolean forValue) {
        daylightSavingsBegin = forValue;
    }

    /**
     * @return Daylight savings end.
     */
    public final boolean getDaylightSavingsEnd() {
        return daylightSavingsEnd;
    }

    /**
     * @param forValue
     *            Daylight savings end.
     */
    public final void setDaylightSavingsEnd(final boolean forValue) {
        daylightSavingsEnd = forValue;
    }

    /**
     * @return Deviation is time from current time zone to UTC time.
     */
    public final int getDeviation() {
        int value = -((meterCalendar.get(Calendar.ZONE_OFFSET)
                + meterCalendar.get(Calendar.DST_OFFSET)) / 60000);
        return value;
    }

    /**
     * @param forValue
     *            Deviation is time from current time zone to UTC time.
     * @deprecated use {@link #setMeterCalendar} instead.
     */
    @Deprecated
    public final void setDeviation(final int forValue) {
        meterCalendar = Calendar.getInstance(getTimeZone(forValue, true));
    }

    /**
     * @return Clock status.
     */
    public final java.util.Set<ClockStatus> getStatus() {
        return status;
    }

    /**
     * @param forValue
     *            Clock status.
     */
    public final void setStatus(final java.util.Set<ClockStatus> forValue) {
        status = forValue;
    }

    public String toFormatString() {
        SimpleDateFormat sd = new SimpleDateFormat();
        int pos;
        if (skip.size() != 0) {
            String dateSeparator = null;
            String timeSeparator = ":";
            // Separate date and time parts.
            List<String> tmp = GXCommon.split(sd.toPattern(), " ");
            List<String> shortDatePattern = new ArrayList<String>();
            List<String> shortTimePattern = new ArrayList<String>();
            // Find date time separator.
            char separator = 0;
            int dateIndex = 0;
            int timeIndex = 1;

            if (Locale.getDefault() == Locale.KOREAN) {
                separator = ' ';
                dateSeparator = String.valueOf(separator);
                tmp.clear();
                tmp.add("yy. M. d");
                tmp.add("a");
                tmp.add("h:mm");
            } else {
                if (tmp.get(0).contains("H")) {
                    dateIndex = 1;
                    timeIndex = 0;
                }
                for (char it : tmp.get(dateIndex).toCharArray()) {
                    if (dateSeparator == null && !Character.isLetter(it)) {
                        dateSeparator = String.valueOf(it);
                    } else if (!Character.isLetter(it)) {
                        separator = it;
                        break;
                    }
                }

            }
            String sep = String.valueOf(separator);
            shortDatePattern.addAll(GXCommon.split(tmp.get(dateIndex), sep));
            boolean amPmFirst = false;
            if ("a".compareToIgnoreCase(tmp.get(1)) == 0) {
                amPmFirst = true;
                shortTimePattern
                        .addAll(GXCommon.split(tmp.get(1 + timeIndex), ":"));
            } else {
                shortTimePattern
                        .addAll(GXCommon.split(tmp.get(timeIndex), ":"));
            }
            if (!shortTimePattern.contains("ss")) {
                shortTimePattern.add("ss");
            }
            if (this instanceof GXTime) {
                shortDatePattern.clear();
            } else {
                if (skip.contains(DateTimeSkips.YEAR)) {
                    pos = shortDatePattern.indexOf("yyyy");
                    if (pos == -1) {
                        pos = shortDatePattern.indexOf("yy");
                    }
                    shortDatePattern.set(pos, "*");
                }
                if (skip.contains(DateTimeSkips.MONTH)) {
                    pos = shortDatePattern.indexOf("M");
                    if (pos == -1) {
                        pos = shortDatePattern.indexOf("MM");
                    }
                    shortDatePattern.set(pos, "*");
                }
                if (skip.contains(DateTimeSkips.DAY)) {
                    pos = shortDatePattern.indexOf("d");
                    shortDatePattern.set(pos, "*");
                }
            }
            if (this instanceof GXDate) {
                shortTimePattern.clear();
            } else {
                if (skip.contains(DateTimeSkips.HOUR)) {
                    pos = shortTimePattern.indexOf("h");
                    if (pos == -1) {
                        pos = shortTimePattern.indexOf("H");
                    }
                    shortTimePattern.set(pos, "*");
                }
                if (skip.contains(DateTimeSkips.MINUTE)) {
                    pos = shortTimePattern.indexOf("mm");
                    shortTimePattern.set(pos, "*");
                }
                if (skip.contains(DateTimeSkips.SECOND)
                        || (shortTimePattern.size() == 1 && getLocalCalendar()
                                .get(Calendar.SECOND) == 0)) {
                    pos = shortTimePattern.indexOf("ss");
                    shortTimePattern.set(pos, "*");
                }
            }
            String format = null;
            if (!shortDatePattern.isEmpty()) {
                format = String.join(dateSeparator,
                        shortDatePattern.toArray(new String[0]));
            }
            if (!shortTimePattern.isEmpty()) {
                if (format != null) {
                    format += " ";
                } else {
                    format = "";
                }
                if (amPmFirst) {
                    format += "a ";
                }
                format += String.join(timeSeparator,
                        shortTimePattern.toArray(new String[0]));
            }
            if (format == "H") {
                return String
                        .valueOf(getLocalCalendar().get(Calendar.HOUR_OF_DAY));
            }
            if (format == null) {
                return "";
            }
            // If AM/PM is used.
            if (!amPmFirst && shortTimePattern.size() > 0 && tmp.size() > 2) {
                format += " a";
            }
            sd = new SimpleDateFormat(format);
            return sd.format(getLocalCalendar().getTime());
        }
        return sd.format(getLocalCalendar().getTime());
    }

    @Override
    public final String toString() {
        SimpleDateFormat sd = new SimpleDateFormat();
        if (!getSkip().isEmpty()) {
            // Separate date and time parts.
            List<String> tmp = GXCommon.split(sd.toPattern(), " ");
            List<String> date = new ArrayList<String>();
            List<String> tm = new ArrayList<String>();
            // Find date time separator.
            char separator = 0;
            if (Locale.getDefault() == Locale.KOREAN) {
                separator = ' ';
                tmp.clear();
                tmp.add("yy. M. d");
                tmp.add("a");
                tmp.add("h:mm");
            } else {
                for (char it : tmp.get(0).toCharArray()) {
                    if (!Character.isLetter(it)) {
                        separator = it;
                        break;
                    }
                }
            }
            boolean amPmFirst = false;
            if (separator != 0) {
                String sep = String.valueOf(separator);
                date.addAll(GXCommon.split(tmp.get(0), sep));
                if ("a".compareToIgnoreCase(tmp.get(1)) == 0) {
                    amPmFirst = true;
                    tm.addAll(GXCommon.split(tmp.get(2), ":"));
                } else {
                    tm.addAll(GXCommon.split(tmp.get(1), ":"));
                }
                if (getSkip().contains(DateTimeSkips.YEAR)) {
                    date.remove("yyyy");
                }
                if (getSkip().contains(DateTimeSkips.MONTH)) {
                    date.remove("M");
                }
                if (getSkip().contains(DateTimeSkips.DAY)) {
                    date.remove("d");
                }
                if (getSkip().contains(DateTimeSkips.HOUR)) {
                    tm.remove("H");
                    tm.remove("HH");
                }
                if (getSkip().contains(DateTimeSkips.MINUTE)) {
                    tm.remove("m");
                    tm.remove("mm");
                }
                if (getSkip().contains(DateTimeSkips.SECOND)) {
                    tm.remove("ss");
                } else {
                    tm.add("ss");
                }
                if (getSkip().contains(DateTimeSkips.MILLISECOND)) {
                    tm.remove("SSS");
                } else {
                    tm.add("SSS");
                }
                String format = "";
                StringBuilder sb = new StringBuilder();
                if (!date.isEmpty()) {
                    for (String it : date) {
                        if (sb.length() != 0) {
                            sb.append(separator);
                        }
                        sb.append(it);
                    }
                    format = sb.toString();
                }
                if (!tm.isEmpty()) {
                    sb.setLength(0);
                    for (String it : tm) {
                        if (sb.length() != 0) {
                            sb.append(':');
                        }
                        sb.append(it);
                    }
                    if (format.length() != 0) {
                        format += " ";
                    }
                    if (amPmFirst && tmp.size() > 2) {
                        format += tmp.get(1) + " ";
                    }
                    format += sb.toString();
                }
                // If 12 hour format. Add AM or PM.
                if (!amPmFirst && tmp.size() > 2) {
                    format += " " + tmp.get(2);
                }

                sd = new SimpleDateFormat(format);
                return sd.format(getLocalCalendar().getTime());
            }
        }
        return sd.format(getLocalCalendar().getTime());
    }

    /**
     * Get difference between given time and run time in ms.
     * 
     * @param start
     *            Start date time.
     * @param to
     *            Compared time.
     * @return Difference in milliseconds.
     */
    public static long getDifference(final Calendar start,
            final GXDateTime to) {
        long diff = 0;
        Calendar cal = to.getLocalCalendar();
        // Compare seconds.
        if (!to.getSkip().contains(DateTimeSkips.SECOND)) {
            if (start.get(Calendar.SECOND) < cal.get(Calendar.SECOND)) {
                diff += (cal.get(Calendar.SECOND) - start.get(Calendar.SECOND))
                        * 1000L;
            } else {
                diff -= (start.get(Calendar.SECOND) - cal.get(Calendar.SECOND))
                        * 1000L;
            }
        } else if (diff < 0) {
            diff = 60000 + diff;
        }
        // Compare minutes.
        if (!to.getSkip().contains(DateTimeSkips.MINUTE)) {
            if (start.get(Calendar.MINUTE) < cal.get(Calendar.MINUTE)) {
                diff += (cal.get(Calendar.MINUTE) - start.get(Calendar.MINUTE))
                        * 60000L;
            } else {
                diff -= (start.get(Calendar.MINUTE) - cal.get(Calendar.MINUTE))
                        * 60000L;
            }
        } else if (diff < 0) {
            diff = 60 * 60000 + diff;
        }
        // Compare hours.
        if (!to.getSkip().contains(DateTimeSkips.HOUR)) {
            if (start.get(Calendar.HOUR_OF_DAY) < cal
                    .get(Calendar.HOUR_OF_DAY)) {
                diff += (cal.get(Calendar.HOUR_OF_DAY)
                        - start.get(Calendar.HOUR_OF_DAY)) * 60 * 60000L;
            } else {
                diff -= (start.get(Calendar.HOUR_OF_DAY)
                        - cal.get(Calendar.HOUR_OF_DAY)) * 60 * 60000L;
            }
        } else if (diff < 0) {
            diff = 60 * 60000 + diff;
        }
        // Compare days.
        if (!to.getSkip().contains(DateTimeSkips.DAY)) {
            if (start.get(Calendar.DAY_OF_MONTH) < cal
                    .get(Calendar.DAY_OF_MONTH)) {
                diff += (cal.get(Calendar.DAY_OF_MONTH)
                        - start.get(Calendar.DAY_OF_MONTH)) * 24 * 60 * 60000;
            } else if (start.get(Calendar.DAY_OF_MONTH) != cal
                    .get(Calendar.DAY_OF_MONTH)) {
                if (!to.getSkip().contains(DateTimeSkips.DAY)) {
                    diff += (cal.get(Calendar.DAY_OF_MONTH)
                            - start.get(Calendar.DAY_OF_MONTH)) * 24 * 60
                            * 60000L;
                } else {
                    diff = ((GXCommon.daysInMonth(start.get(Calendar.YEAR),
                            start.get(Calendar.MONTH))
                            - start.get(Calendar.DAY_OF_MONTH)
                            + cal.get(Calendar.DAY_OF_MONTH)) * 24 * 60
                            * 60000L) + diff;
                }
            }
        } else if (diff < 0) {
            diff = 24 * 60 * 60000 + diff;
        }
        // Compare months.
        if (!to.getSkip().contains(DateTimeSkips.MONTH)) {
            if (start.get(Calendar.MONTH) < cal.get(Calendar.MONTH)) {
                for (int m = start.get(Calendar.MONTH); m != cal
                        .get(Calendar.MONTH); ++m) {
                    diff += GXCommon.daysInMonth(start.get(Calendar.YEAR), m)
                            * 24 * 60 * 60000L;
                }
            } else {
                for (int m = cal.get(Calendar.MONTH); m != start
                        .get(Calendar.MONTH); ++m) {
                    diff += -GXCommon.daysInMonth(start.get(Calendar.YEAR), m)
                            * 24 * 60 * 60000L;
                }
            }
        } else if (diff < 0) {
            diff = GXCommon.daysInMonth(start.get(Calendar.YEAR),
                    start.get(Calendar.MONTH)) * 24 * 60 * 60000L + diff;
        }
        return diff;
    }

    /**
     * Convert deviation to time zone.
     * 
     * @param deviation
     *            Used deviation.
     * @param dst
     *            Is daylight saving time used.
     * @return Time zone.
     */
    public static TimeZone getTimeZone(final int deviation, final boolean dst) {
        // Return current time zone if time zone is not used.
        if (deviation == 0x8000 || deviation == -32768) {
            return Calendar.getInstance().getTimeZone();
        }
        TimeZone tz = Calendar.getInstance().getTimeZone();
        if (dst) {
            // If meter is in same time zone than meter reading application.
            if (tz.observesDaylightTime()
                    && tz.getRawOffset() / 60000 == deviation - 60) {
                return tz;
            }
            String[] ids = TimeZone.getAvailableIDs((deviation - 60) * 60000);
            tz = null;
            for (int pos = 0; pos != ids.length; ++pos) {
                tz = TimeZone.getTimeZone(ids[pos]);
                if (tz.observesDaylightTime()
                        && tz.getRawOffset() / 60000 == deviation - 60) {
                    break;
                }
                tz = null;
            }
            if (tz != null) {
                return tz;
            }
        }
        if (!tz.observesDaylightTime()
                && tz.getRawOffset() / 60000 == deviation) {
            return tz;
        }
        String str;
        DecimalFormat df = new DecimalFormat("00");
        String tmp =
                df.format(deviation / 60) + ":" + df.format(deviation % 60);
        if (deviation == 0) {
            str = "GMT";
        } else if (deviation > 0) {
            str = "GMT+" + tmp;
        } else {
            str = "GMT" + tmp;
        }
        return TimeZone.getTimeZone(str);
    }

    /**
     * Get date time from Epoch time.
     * 
     * @param unixTime
     *            Unix time.
     * @return Date and time.
     */
    public static GXDateTime fromUnixTime(final long unixTime) {
        return new GXDateTime(new Date(unixTime * 1000));
    }

    /**
     * Convert date time to Epoch time.
     * 
     * @param date
     *            Date and time.
     * @return Unix time.
     */
    public static long toUnixTime(final java.util.Date date) {
        return date.getTime() / 1000;
    }

    /**
     * Convert date time to Epoch time.
     * 
     * @param date
     *            Date and time.
     * @return Unix time.
     */
    public static long toUnixTime(final GXDateTime date) {
        return date.getLocalCalendar().getTime().getTime() / 1000;
    }
}