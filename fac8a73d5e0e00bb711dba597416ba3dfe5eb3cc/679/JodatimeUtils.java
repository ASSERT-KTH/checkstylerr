package org.onetwo.common.utils;


import java.util.Date;
import java.util.Objects;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.onetwo.common.date.DateUtils;

public class JodatimeUtils {
	
	public static String[] DATE_PATTERNS = new String[]{"yyyy-MM-dd"};
	public static String PATTERN_DATE_TIME = "yyyy-MM-dd HH:mm:ss";

	public static DateTime atStartOfDate(Date date){
		return atStartOfDate(date, 0);
	}
	public static DateTime atStartOfDate(Date date, int plusDays){
		Objects.requireNonNull(date);
//		DateTime dt = new DateTime(date).withTimeAtStartOfDay();
		DateTime dt = new DateTime(date).plusDays(plusDays).millisOfDay().withMinimumValue();
		return dt;
	}
	
	public static DateTime atEndOfDate(Date date){
		return atEndOfDate(date, 0);
	}
	public static DateTime atEndOfDate(Date date, int plusDays){
		Objects.requireNonNull(date);
//		DateTime dt = new DateTime(date).withTimeAtStartOfDay().plusDays(1).minusMillis(1);
		DateTime dt = new DateTime(date).plusDays(plusDays).millisOfDay().withMaximumValue();
		return dt;
	}
	
	public static DateTime atStartOfMonth(Date date){
		Objects.requireNonNull(date);
		DateTime dt = new DateTime(date).dayOfMonth().withMinimumValue();
//		return dt.toDate();
		return dt;
	}
	
	public static DateTime atEndOfMonth(Date date){
		Objects.requireNonNull(date);
		DateTime dt = new DateTime(date).dayOfMonth().withMaximumValue();
		return dt;
	}
	
	public static String format(Date date, String pattern){
		Objects.requireNonNull(date);
		return new DateTime(date).toString(pattern==null?PATTERN_DATE_TIME:pattern);
	}
	public static String formatDateTime(Date date){
		Objects.requireNonNull(date);
		return new DateTime(date).toString(PATTERN_DATE_TIME);
	}
	
	public static DateTime parse(String source, String pattern){
		Assert.hasText(source);
		return DateTimeFormat.forPattern(pattern).parseDateTime(source);
	}
	
	public static DateTime parse(String source){
		Assert.hasText(source);
		return DateTimeFormat.forPattern(DateUtils.matchPattern(source)).parseDateTime(source);
	}

	public static Interval createInterval(String start, String end){
		return new Interval(parse(start), parse(end));
	}
	
	public static Interval createInterval(Date start, Date end){
		Objects.requireNonNull(start);
		Objects.requireNonNull(end);
		Interval interval = new Interval(new DateTime(start), new DateTime(end));
		return interval;
	}
	
	/****
	 * theTime是否在start和end的时间内
	 * @param theTime
	 * @param start
	 * @param end
	 * @return
	 * @author way
	 */
	public static boolean isTimeBetweenInterval(Date theTime, Date start, Date end){
		Objects.requireNonNull(start);
		Objects.requireNonNull(end);
		Interval interval = createInterval(start, end);
		return interval.contains(new DateTime(theTime));
	}

	public static Period between(Date start, Date end){
		Period p = new Period(start.getTime(), end.getTime());
		return p;
	}
	public static Period between(LocalDateTime start, LocalDateTime end){
		Period p = new Period(start, end);
		return p;
	}

	private JodatimeUtils(){
	}
}
