package com.hlj.utils;


import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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


    /**
     * 获取当前的字符串日期
     * @return
     */
    public static String getDateString(){
        return convertDate2String(new Date(), YYYY_MM_DD_HH_MM_SS);
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
     * 时间段转化
     * 比如 ：天数转化为小时  1, Calendar.DATE ,Calendar.HOUR
     * 方法：现将它转化为最低的毫秒为单位，再进行转化
     * @return
     */
    public static Long coverLongToLong(Long start,int startField,int endField){

        Long result =0L ;
        Long startMillis = 0L;
        //小时转分钟
        switch (startField){
            case Calendar.DATE:
                startMillis = 1000 * 60 * 60 * 24 * start ;
                break;
            case Calendar.HOUR:
                startMillis = 1000 * 60 *  60 * start ;
                break;
            case Calendar.MINUTE:
                startMillis = 1000 * 60  * start ;
                break;
            case Calendar.SECOND:
                startMillis = 1000 * start ;
                break;
            case Calendar.MILLISECOND: //毫秒
                startMillis = start ;
                break;
            default:
                throw new RuntimeException("没有找到匹配项");
        }

        switch (endField){
            case Calendar.DATE:
                result =  startMillis / (1000 * 60 * 60 * 24);
                break;
            case Calendar.HOUR:
                result = startMillis / (1000 * 60 * 60 );
                break;
            case Calendar.MINUTE:
                result = startMillis / (1000 * 60);
                break;
            case Calendar.SECOND:
                result= startMillis / (1000);
                break;
            case Calendar.MILLISECOND: //毫秒
                result = startMillis ;
                break;
            default:
                break;
        }

        return result ;
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
     * 获取两个日期之间的所有分钟数
     * @param startDate   开始日期
     * @param endDate     结束日期
     * @return      num 为分钟间隔
     */
    public static List<String> getMinutes( Date startDate, Date endDate,Integer num) {
        List<String> result = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
        startCalendar.setTime(startDate);
        startCalendar.set(Calendar.SECOND, 0); //清空它的秒
        startCalendar.set(Calendar.MILLISECOND, 0);

        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        result.add(df.format(startCalendar.getTime()));
        while(true){
            startCalendar.add(Calendar.MINUTE, num);
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
        if(weeks >=2 && weeks <=6){
            flag = true;
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
     * 获取最近前或者后 多少分钟的开始时间
     * @param num
     * @return
     */
    public static Date getRecentMinuteFirstTime(Date date,Integer num){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, num);
        String time  = DateHelper.convertDate2String(calendar.getTime(), DateHelper.YYYY_MM_DD_HH_MM)+":00";
        return DateHelper.convertString2Date(time,DateHelper.YYYY_MM_DD_HH_MM_SS);
    }


    /**
     * 获取最近前或者后 多少分钟的时间
     * @param num
     * @return
     */
    public static Date getRecentMinute(Date date,Integer num){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, num);
        return calendar.getTime();
    }



    /**
     * 获取最近前或者后 多少小时的时间
     * @param num
     * @return
     */

    public static Date getRecentHour(Date date, Integer num){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.HOUR, num);
        return calendar.getTime();
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
        long distanceHour = hour+day*24;
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long distanceMin = min+distanceHour*60;
        sec = (diff/1000-day*24*60*60-hour*60*60-min*60);
        long distanceSec = sec+distanceMin*60;

        System.out.println(day+"天"+hour+"小时"+min+"分钟"+sec+"秒");
        System.out.println(day+"天"+distanceHour+"小时"+distanceMin+"分钟"+distanceSec+"秒");

        Long[] times = {day, distanceHour, distanceMin, distanceSec};
        return times;
    }




    /**
     * 判断time是否在from，to之内
     * Date1.after(Date2),  当Date1大于Date2时，返回TRUE，当小于等于时，返回false； date1是现在日期 date2是过去日期
     * Date1.before(Date2)，当Date1小于Date2时，返回TRUE，当大于等于时，返回false；
     * @param time 指定日期
     * @param from 开始日期
     * @param to   结束日期
     * @return
     */
    public static boolean belongCalendar(Date time, Date from, Date to) {
        Calendar date = Calendar.getInstance();
        date.setTime(time);
        Calendar after = Calendar.getInstance();
        after.setTime(from);
        Calendar before = Calendar.getInstance();
        before.setTime(to);
        if (date.after(after) && date.before(before)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 当前时间加多少秒
     * @param date
     * @return
     */
    public static Date addSeconds(Date date,int num){
        return DateUtils.addSeconds(date, num);
    }




}
