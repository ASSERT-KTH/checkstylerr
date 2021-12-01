package com.databasepreservation.visualization.shared.client.Tools;

import java.util.Date;

import com.google.gwt.i18n.client.NumberFormat;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class Humanize {
  public static final String BYTES = "B";
  public static final String KILOBYTES = "KB";
  public static final String MEGABYTES = "MB";
  public static final String GIGABYTES = "GB";
  public static final String TERABYTES = "TB";
  public static final String PETABYTES = "PB";

  public static final String[] UNITS = new String[] {BYTES, KILOBYTES, MEGABYTES, GIGABYTES, TERABYTES, PETABYTES};

  public static final double BYTES_IN_KILOBYTES = 1024L;
  public static final double BYTES_IN_MEGABYTES = 1048576L;
  public static final double BYTES_IN_GIGABYTES = 1073741824L;
  public static final double BYTES_IN_TERABYTES = 1099511627776L;
  public static final double BYTES_IN_PETABYTES = 1125899906842624L;

  public static final double[] BYTES_IN_UNITS = {1, BYTES_IN_KILOBYTES, BYTES_IN_MEGABYTES, BYTES_IN_GIGABYTES,
    BYTES_IN_TERABYTES, BYTES_IN_PETABYTES};

  protected static final NumberFormat SMALL_NUMBER_FORMAT = NumberFormat.getFormat("0.#");
  protected static final NumberFormat NUMBER_FORMAT = NumberFormat.getFormat("#");

  public static Long parseFileSize(String size, String unit) {
    Long ret = null;
    if (size != null && !size.isEmpty()) {
      size = size.trim();
      if (unit.equals(PETABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_PETABYTES);
      } else if (unit.equals(TERABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_TERABYTES);
      } else if (unit.equals(GIGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_GIGABYTES);
      } else if (unit.equals(MEGABYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_MEGABYTES);
      } else if (unit.equals(KILOBYTES)) {
        ret = Math.round(Double.parseDouble(size) * BYTES_IN_KILOBYTES);
      } else if (unit.equals(BYTES)) {
        ret = Long.parseLong(size);
      } else {
        throw new IllegalArgumentException(size);
      }
    }
    return ret;
  }

  public static String readableFileSize(long size) {
    if (size <= 0) {
      return "0 B";
    }
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return NumberFormat.getFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + UNITS[digitGroups];
  }

  public static String getDatesText(Date dateInitial, Date dateFinal, boolean extendedDate) {
    String ret;

    // if (dateInitial == null && dateFinal == null) {
    // ret = extendedDate ? messages.titleDatesEmpty() :
    // messages.simpleDatesEmpty();
    // } else if (dateInitial != null && dateFinal == null) {
    // ret = extendedDate ? messages.titleDatesNoFinal(dateInitial) :
    // messages.simpleDatesNoFinal(dateInitial);
    // } else if (dateInitial == null && dateFinal != null) {
    // ret = extendedDate ? messages.titleDatesNoInitial(dateFinal) :
    // messages.simpleDatesNoInitial(dateFinal);
    // } else {
    // ret = extendedDate ? messages.titleDates(dateInitial, dateFinal) :
    // messages.simpleDates(dateInitial, dateFinal);
    // }

    return "change-me!"; // TODO: i18n
  }

  public static String durationInDHMS(Date start, Date end) {
    if (end == null) {
      end = new Date();
    }
    return durationMillisToDHMS(end.getTime() - start.getTime());
  }

  public static String durationInShortDHMS(Date start, Date end) {
    if (end == null) {
      end = new Date();
    }
    return durationMillisToShortDHMS(end.getTime() - start.getTime());
  }

  public final static long ONE_SECOND = 1000;
  public final static long SECONDS = 60;

  public final static long ONE_MINUTE = ONE_SECOND * 60;
  public final static long MINUTES = 60;

  public final static long ONE_HOUR = ONE_MINUTE * 60;
  public final static long HOURS = 24;

  public final static long ONE_DAY = ONE_HOUR * 24;

  /**
   * converts time (in milliseconds) to human-readable format
   * "<w> days, <x> hours, <y> minutes and (z) seconds"
   */
  public static String durationMillisToDHMS(long duration) {
    StringBuffer res = new StringBuffer();
    long temp = 0;
    if (duration >= ONE_SECOND) {
      // temp = duration / ONE_DAY;
      // if (temp > 0) {
      // duration -= temp * ONE_DAY;
      // res.append(messages.durationDHMSDay((int) temp)).append(
      // duration >= ONE_MINUTE ? messages.durationDHMSSeparator() : "");
      // }
      //
      // temp = duration / ONE_HOUR;
      // if (temp > 0) {
      // duration -= temp * ONE_HOUR;
      // res.append(messages.durationDHMSHour((int) temp)).append(
      // duration >= ONE_MINUTE ? messages.durationDHMSSeparator() : "");
      // }
      //
      // temp = duration / ONE_MINUTE;
      // if (temp > 0) {
      // duration -= temp * ONE_MINUTE;
      // res.append(messages.durationDHMSMinutes((int) temp));
      // }
      //
      // if (!res.toString().equals("") && duration >= ONE_SECOND) {
      // res.append(messages.durationDHMSSecondSeparator());
      // }
      //
      // temp = duration / ONE_SECOND;
      // if (temp > 0) {
      // res.append(messages.durationDHMSSeconds((int) temp));
      // }
      return "change-me!"; // TODO: i18n
    } else {
      return "change-me!";// messages.durationDHMSLessThanASecond();
    }
  }

  /**
   * converts time (in milliseconds) to human-readable format "<dd:>hh:mm:ss"
   */
  public static String durationMillisToShortDHMS(long duration) {
    duration /= ONE_SECOND;
    int seconds = (int) (duration % SECONDS);
    duration /= SECONDS;
    int minutes = (int) (duration % MINUTES);
    duration /= MINUTES;
    int hours = (int) (duration % HOURS);
    int days = (int) (duration / HOURS);

    String ret;

    // if (days > 0) {
    // ret = messages.durationDHMSShortDays(days, hours, minutes, seconds);
    // } else if (hours > 0) {
    // ret = messages.durationDHMSShortHours(hours, minutes, seconds);
    // } else if (minutes > 0) {
    // ret = messages.durationDHMSShortMinutes(minutes, seconds);
    // } else {
    // ret = messages.durationDHMSShortSeconds(seconds);
    // }

    return "change-me!"; // TODO: i18n
  }
}
