package com.hlj.spring.boot.docker.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by j.sh on 2015/5/15.
 */
public class DateHelper extends DateUtils {

    private static final Logger logger = LoggerFactory.getLogger(DateHelper.class);

    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";

    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    public static final String YYYYMMDD_SLANT = "yyyy/MM/dd";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYY = "yyyy";
    public static final String MMDD = "MMdd";
    public static final String HHMMSS = "HHmmss";


    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    public static final String HH_MM = "HH:mm";
    public static Date convertString2Dat;

    /**
     * 格式化日期为字符串
     * @param date
     * @param pattern
     * @return
     */
    public static String convertDate2String(Date date,String pattern){
        if (date == null)
            return null;

        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    public static String convertDate2String(Date date){
        return convertDate2String(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 转换字符串为日期
     * @param pattern 日期格式
     * @param dateStr 日期字符串
     * @return date
     */
    public static Date convertString2Date(String dateStr,String pattern ){
        if(StringUtils.isBlank(dateStr)){
            throw new NullPointerException("dateStr is null");
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            return new Date(df.parse(dateStr).getTime());
        } catch (ParseException pe) {
            logger.error(pe.getMessage(),pe);
            throw new RuntimeException("date parse error"+dateStr);
        }
    }


    public static String formatDateString(String dateStr,String pattern){
        if(StringUtils.isBlank(dateStr)){
            throw new NullPointerException("dateStr is null");
        }
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        try {
            Date date = new Date(df.parse(dateStr).getTime());
            return df.format(date);
        } catch (ParseException pe) {
            logger.error(pe.getMessage(),pe);
            throw new RuntimeException("date parse error"+dateStr);
        }
    }

    /**
     * 滚动日期,
     * @param point 要滚动的时间
     * @param field 要滚动的类别,传入参数为Calendar.YEAR,Calendar.MONTH,DATE...等
     * @param num 滚动数量,正负数都可以,负数为向前滚动,
     * @return  结果
     * 例: DateHelper.rollDate(new Date(),Calendar.DATE,-12);  当前日期的前12天
     * 例: DateHelper.rollDate(new Date(),Calendar.MONTH,2);  向后滚动两个月
     */
    public static Date rollDate(Date point,int field,int num){
        Calendar cal = Calendar.getInstance();
        cal.setTime(point);
        cal.add(field,num);
        return cal.getTime();
    }


    /**
     * 获取当前小时最初的时间
     * @param date
     * @return
     */
    public static Date getHourFirstTime(Date date){
        if(date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取当前小时最后的时间
     * @param date
     * @return
     */
    public static Date getHourLastTime(Date date){
        if(date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND,0);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 得到一天的最早的时间
     * @param date one day
     * @return date
     */
    public static Date getDateFirstTime(Date date){
        if(date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Date(cal.getTimeInMillis());
    }

    /**
     * 获取一天最晚的时间
     * @param date one day
     * @return date
     */
    public static Date getDateLastTime(Date date){
        if(date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }


    /**
     * 获取传入日期所在月份的开始日期
     * @param date  date
     * @return 日期所在月份的开始日期
     */
    public static Date getMonthStartTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE,cal.getActualMinimum(Calendar.DATE));
        return getDateFirstTime(cal.getTime());
    }

    /**
     * 获取传入日期所在月份的结束日期
     * @param date  date
     * @return 日期所在月份的结束日期
     */
    public static Date getMonthEndTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE,cal.getActualMaximum(Calendar.DATE));
        return getDateLastTime(cal.getTime());
    }

    /**
     * 获取当年最后时间
     * @param date
     * @return
     */
    public static Date getYearLastTime(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH,Calendar.DECEMBER);
        calendar.set(Calendar.DAY_OF_YEAR,calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
        return getDateLastTime(calendar.getTime());
    }

    /**
     * 获取传入日期的上个月的开始日期
     * @param date  date
     * @return 上一个月的开始日期
     */
    public static Date getLastMonthStartTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        //设置为1号,当前日期既为本月第一天
        cal.set(Calendar.DATE,cal.getActualMinimum(Calendar.DATE));
        return getDateFirstTime(cal.getTime());
    }

    /**
     * 获取传入日期上个月的结束日期
     * @param date  date
     * @return 日期所在月份的结束日期
     */
    public static Date getLastMonthEndTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);
        //设置为1号,当前日期既为本月第一天
        cal.set(Calendar.DATE,cal.getActualMaximum(Calendar.DATE));
        return getDateLastTime(cal.getTime());
    }

    /**
     * 获取传入日期的上个周的开始日期
     * @param date  date
     * @return 上一个周的开始日期  认为是周一
     */
    public static Date getLastWeekStartTime(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.WEEK_OF_YEAR,-1);
        return getThisWeekStartTime(calendar.getTime());
    }

    /**
     * 获取传入日期的昨天的日期
     * @param date  date
     * @return 昨天
     */
    public static Date getYesterDayStartTime(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, -1);
        //设置为1号,当前日期既为本月第一天
        return getDateFirstTime(cal.getTime());
    }

    /**
     * 获取传入日期上个周的结束日期
     * @param date  date
     * @return 日期所在周的结束日期
     */
    public static Date getLastWeekEndTime(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.WEEK_OF_YEAR,-1);
        return getThisWeekEndTime(calendar.getTime());
    }


    /**
     * 获取传入日期的本周的开始日期
     * @param date  date
     * @return 上一个周的开始日期  认为是周一
     */
    public static Date getThisWeekStartTime(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek());
        return getDateFirstTime(calendar.getTime());
    }

    /**
     * 获取传入日期本周的结束日期
     * @param date  date
     * @return 日期所在周的结束日期
     */
    public static Date getThisWeekEndTime(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_WEEK,
                calendar.getFirstDayOfWeek() + 6);
        return getDateLastTime(calendar.getTime());
    }

    /**
     * 获取hour
     * @param date one day
     * @return date
     */
    public static int getDateHour(Date date){
        if(date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 获取分钟
     * @param date one day
     * @return date
     */
    public static int getDateMinute(Date date){
        if(date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.MINUTE);
    }

    /**
     * 通过 long 得到Date
     * @param date one day
     * @return date
     */
    public static Date getDateByTime(Long date){
        if(date == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new Date(cal.getTimeInMillis());
    }

    /**
     * 第二个时间相对第一个时间的天数差
     * @param date1
     * @param date2
     * @return
     */
    public static long differ(Date date1, Date date2){
        return date2.getTime() / 86400000 - date1.getTime() / 86400000;
    }

    public static long differDays(Date date1, Date date2){
        return Days.daysBetween(new DateTime(date1).withTimeAtStartOfDay(),new DateTime(date2).withTimeAtStartOfDay()).getDays();
    }

    public static long differSeconds(Date date1, Date date2){
        return Seconds.secondsBetween(new DateTime(date1),new DateTime(date2)).getSeconds();
    }

    public static long differMinutes(Date date1, Date date2){
        return Minutes.minutesBetween(new DateTime(date1).withSecondOfMinute(0),new DateTime(date2).withSecondOfMinute(0)).getMinutes();
    }

    public static long differHours(Date date1, Date date2){
        return Hours.hoursBetween(new DateTime(date1),new DateTime(date2)).getHours();
    }

    public static double differDoubleHours(Date date1, Date date2){
        return BigDecimal.valueOf(date2.getTime() - date1.getTime()).divide(BigDecimal.valueOf(3600000),1,BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        return StringUtils.equals(convertDate2String(date1,DateHelper.YYYYMMDD),convertDate2String(date2,DateHelper.YYYYMMDD));
    }

    /**
     * 得到一个理论上软件生命周期无法到达的时间
     * @return date
     */
    public static Date getRemoteDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2999);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 31);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    /**
     * 得到一个多点广告开始的时间
     * @return date
     */
    public static Date getInitDate(){
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2016);
        cal.set(Calendar.MONTH, 11);
        cal.set(Calendar.DATE, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }


    /**
     * 获取两个日期之间的所有日期
     * @param pattern    日期格式
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return      Set<String>
     */
    public static List<String> getDates(String pattern, Date startDate, Date endDate) {
        List<String> result = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        result.add(df.format(startCalendar.getTime()));
        while(true){
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
            if(startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()){
                result.add(df.format(startCalendar.getTime()));
            }else{
                break;
            }
        }
        return result;
    }

    /**
     * 获取两个日期之间的所有周的周一日期
     * @param pattern    日期格式
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return      Set<String>
     */
    public static List<String> getWeeksMonday(String pattern, Date startDate, Date endDate) {
        List<String> result = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        startCalendar.setTime(getThisWeekStartTime(startDate));
        startCalendar.setFirstDayOfWeek(Calendar.MONDAY);
        endCalendar.setTime(endDate);
        result.add(df.format(getThisWeekStartTime(startDate)));
        while(true){
            startCalendar.add(Calendar.WEEK_OF_YEAR, 1);
            if(startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()){
                result.add(df.format(startCalendar.getTime()));
            }else{
                break;
            }
        }
        return result;
    }

    /**
     * 根据ios回传的时间获取正常时间
     * @param apptime
     * @return
     */
    public static Date getValidDateFromIOS(Long apptime){
        try {
            return new Date((Long.valueOf(apptime)+978307200)*1000L);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取传入日期对应月份 总共跨越了几个周
     * @param date
     * @return
     */
    public static int getMaxWeekNumOfMonth(Date date){
        if(date == null) return 0;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        return cal.getActualMaximum(Calendar.WEEK_OF_MONTH);
    }

    /**
     * 日期在一年中第几周
     * @param date
     * @return
     */
    public static int getWhichWeek(Date date) throws Exception {
        if(date == null) return 0;

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(date);
        calendar1.setFirstDayOfWeek(Calendar.MONDAY);
        return calendar1.get(Calendar.WEEK_OF_YEAR);

    }

    /**
     * 获取两个日期 跨越了几个周
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getWeekNum(Date startDate, Date endDate){
        if(startDate == null || endDate == null) return 0;

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(startDate);
        calendar1.setFirstDayOfWeek(Calendar.MONDAY);
        Integer w1 = calendar1.get(Calendar.WEEK_OF_YEAR);

        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(endDate);
        calendar2.setFirstDayOfWeek(Calendar.MONDAY);
        Integer w2 = calendar2.get(Calendar.WEEK_OF_YEAR);

        return w2 - w1 + 1;
    }

    /**
     * 检查当前时间是否为工作日（周一至周五）
     * @return
     */
    public static boolean checkCurrentDayIsWorkDay(){
        boolean flag = false;
        Calendar curr_calendar = Calendar.getInstance();
        int weeks = curr_calendar.get(Calendar.DAY_OF_WEEK);

//        Integer start_minute = 9 * 60;
//        Integer end_minute = 18 * 60;

//        Integer current_minute = curr_calendar.get(Calendar.HOUR_OF_DAY) * 60 + curr_calendar.get(Calendar.MINUTE);

        if(weeks >=2 && weeks <=6){
//        if(current_minute >= start_minute && current_minute <= end_minute && ( weeks >=2 && weeks <=6 )){
            flag = true;
//            System.out.println(String.format("当前时间: [%s] 在工作日9点~18点范围内",curr_calendar.getTime()));
        }
        return flag;
    }



    /**
     * 获取最近前多少天的开始时间
     * @param num
     * @return
     */
    public  static Date  getRecentlyDayFirstTime(Integer num) {
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, num);
        String time  = DateHelper.convertDate2String(calendar.getTime(), DateHelper.YYYY_MM_DD)+" 00:00:00";
        return DateHelper.convertString2Date(time,DateHelper.YYYY_MM_DD_HH_MM_SS);
    }





    /**
     * 获取最近前或者后 多少分钟的开始时间
     * @param num
     * @return
     */
    public static Date getRecentMinuteFirstTime(Integer num){
        Date date  = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, num);
        String time  = DateHelper.convertDate2String(calendar.getTime(), DateHelper.YYYY_MM_DD_HH_MM)+":00";
        return DateHelper.convertString2Date(time,DateHelper.YYYY_MM_DD_HH_MM_SS);
    }


    /**
     * 输入long 转化date或者string
     * @param s
     * @return
     */
    public Map<String,Object> stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        long lt = new Long(s);
        Date date = new Date(lt);
        Map<String,Object> map = new HashMap<>();
        map.put("date",date);
        res = simpleDateFormat.format(date);
        map.put("dateString",res);

        return map;
    }



    /**
     * 两个时间之间相差距离多少天
     * @param one 时间参数 1：
     * @param two 时间参数 2：
     * @return 相差天数
     */
    public static Long getDistanceDays(Date one, Date two){
        one = DateHelper.getDateFirstTime(one);
        two = DateHelper.getDateFirstTime(two);
        long days=0;
        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff ;
        if(time1<time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        days = diff / (1000 * 60 * 60 * 24);
        return days;
    }


    /**
     * 两个时间相差距离多少天多少小时多少分多少秒
     * @return Long[] 返回值为：{天, 时, 分, 秒}
     */
    public static Long[] getDistanceTimes(Date one, Date two) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff ;
        if(time1<time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff/1000-day*24*60*60-hour*60*60-min*60);

        System.out.println(day+"天"+hour+"小时"+min+"分钟"+sec+"秒");

        Long[] times = {day, hour, min, sec};
        return times;
    }




}
