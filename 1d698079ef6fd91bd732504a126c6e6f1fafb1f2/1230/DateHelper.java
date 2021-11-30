package com.hlj.util.Z012日期.D02Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;
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
    public static final String YYYY_MM_DD_HH = "yyyy-MM-dd HH";
    public static final String YYYY_MM_DD_HH_MM = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    public static final String YYYY = "yyyy";
    public static final String MMDD = "MMdd";
    public static final String HH_MM = "HH:mm";
    public static final String HHMMSS = "HHmmss";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHH = "yyyyMMddHH";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYYMMDD_SLANT = "yyyy/MM/dd";


    /**
     * 格式化日期为字符串
     *
     * @param date    日期
     * @param pattern 格式
     * @return
     */
    public static String toDateString(Date date, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(date);
    }

    /**
     * 转换字符串为日期
     *
     * @param pattern 日期格式
     * @param dateStr 日期字符串
     * @return date
     */
    public static Date toDate(String dateStr, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * 滚动日期 加减日期
     *
     * @param date  要操作的时间
     * @param field 要滚动的类别,传入参数为Calendar.YEAR,Calendar.MONTH,DATE...等
     * @param num   滚动数量,正负数都可以,负数为向前滚动,
     * @return 结果
     * 例: DateHelper.rollDate(new Date(),Calendar.DATE,-12); 当前日期的前12天
     * 例: DateHelper.rollDate(new Date(),Calendar.MONTH,2);  向后滚动两个月
     */
    public static Date rollDate(Date date, int field, int num) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(field, num);
        return cal.getTime();
    }


    /**
     * 获取某个时间的最初时间
     * 目前支持
     *
     * @param date  日期
     * @param field 例如：Calendar.MINUTE
     *              比如: , new Date() ,Calendar.HOUR ->获取该日期小时最初时间  12:22:23 - > 12:00:00
     * @return
     */
    public static Date getDateFirstTime(Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch (field) {
            //1、获取小时的最初时间
            case Calendar.HOUR:
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.DATE:
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.MONTH:
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.YEAR:
                cal.set(Calendar.MONTH, Calendar.JANUARY);
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMinimum(Calendar.DAY_OF_YEAR));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            default:
                throw new RuntimeException("field 无日期匹配类型");

        }
    }


    /**
     * 获取某个时间的最后时间
     * 目前支持
     *
     * @param date  日期
     * @param field 例如：Calendar.MINUTE
     *              比如: , new Date() ,Calendar.HOUR ->获取该日期小时最后时间  12:22 - > 12:59:59
     * @return
     */
    public static Date getDateLastTime(Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        switch (field) {
            //1、获取小时的最后时间
            case Calendar.HOUR:
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.DATE:
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.MONTH:
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            case Calendar.YEAR:
                cal.set(Calendar.MONTH, Calendar.DECEMBER);
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            default:
                throw new RuntimeException("field 无日期匹配类型");

        }
    }


    /**
     * 获取某个时间的位置 比如
     * 1、 Calendar.DAY_OF_YEAR 参数日期是一年中的多少天 传入
     * 2、 Calendar.HOUR  获取的是参数日期的小时 下午5点返回5
     * Calendar.HOUR_OF_DAY  如果需要返回参数日期一天中的多少小时则输入
     */
    public static int getValueOfField(Date date, int field) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(field);
    }


    /**
     * 获取两个日期之间的所有日期
     *
     * @param pattern   日期格式
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return Set<String>
     */
    public static List<String> getDates(String pattern, Date startDate, Date endDate) {
        List<String> result = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        startCalendar.setTime(startDate);
        endCalendar.setTime(endDate);
        result.add(df.format(startCalendar.getTime()));
        while (true) {
            startCalendar.add(Calendar.DAY_OF_MONTH, 1);
            if (startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
                result.add(df.format(startCalendar.getTime()));
            } else {
                break;
            }
        }
        return result;
    }


    /**
     * 获取两个日期之间的所有分钟数
     *
     * @param pattern 开始日期
     * @param endDate 结束日期
     * @return num 为分钟间隔
     */
    public static List<String> getMinutes(String pattern, Date startDate, Date endDate, Integer num) {
        List<String> result = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        startCalendar.setTime(startDate);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        endCalendar.setTime(endDate);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        result.add(df.format(startCalendar.getTime()));
        while (true) {
            startCalendar.add(Calendar.MINUTE, num);
            if (startCalendar.getTimeInMillis() <= endCalendar.getTimeInMillis()) {
                result.add(df.format(startCalendar.getTime()));
            } else {
                break;
            }
        }
        return result;
    }


    /**
     * 两个时间之间相差距离多少天
     *
     * @param one 时间参数 1：
     * @param two 时间参数 2：
     * @return 相差天数
     */
    public static Long getDistanceDays(Date one, Date two) {
        one = DateHelper.getDateFirstTime(one, Calendar.DATE);
        two = DateHelper.getDateFirstTime(two, Calendar.DATE);
        long days = 0;
        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        days = diff / (1000 * 60 * 60 * 24);
        return days;
    }


    /**
     * 两个时间相差距离多少天多少小时多少分多少秒 (实际数据)
     *
     * @return Long[] 返回值为：{天, 时, 分, 秒}
     */
    public static Long[] getDistanceTimes(Date one, Date two) {

        long time1 = one.getTime();
        long time2 = two.getTime();
        long diff;
        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        long day = diff / (24 * 60 * 60 * 1000);
        long hour = (diff / (60 * 60 * 1000) - day * 24);
        long min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        long sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        Long[] times = {day, hour, min, sec};
        return times;
    }


    /**
     * 时间段转化
     * 比如 ：天数转化为小时  1, Calendar.DATE ,Calendar.HOUR
     * 方法：现将它转化为最低的毫秒为单位，再进行转化
     *
     * @return
     */
    public static Long coverFieldTOLong(Long start, int startField, int endField) {
        Long result = 0L;
        Long startMillis = 0L;
        //小时转分钟
        switch (startField) {
            case Calendar.DATE:
                startMillis = 1000 * 60 * 60 * 24 * start;
                break;
            case Calendar.HOUR:
                startMillis = 1000 * 60 * 60 * start;
                break;
            case Calendar.MINUTE:
                startMillis = 1000 * 60 * start;
                break;
            case Calendar.SECOND:
                startMillis = 1000 * start;
                break;
            case Calendar.MILLISECOND:
                startMillis = start;
                break;
            default:
                throw new RuntimeException("没有找到匹配项");
        }

        switch (endField) {
            case Calendar.DATE:
                result = startMillis / (1000 * 60 * 60 * 24);
                break;
            case Calendar.HOUR:
                result = startMillis / (1000 * 60 * 60);
                break;
            case Calendar.MINUTE:
                result = startMillis / (1000 * 60);
                break;
            case Calendar.SECOND:
                result = startMillis / (1000);
                break;
            case Calendar.MILLISECOND:
                result = startMillis;
                break;
            default:
                break;
        }

        return result;
    }


    /**
     * 判断time是否在from，to之内
     * Date1.after(Date2),  当Date1大于Date2时，返回TRUE，当小于等于时，返回false； date1是现在日期 date2是过去日期
     * Date1.before(Date2)，当Date1小于Date2时，返回TRUE，当大于等于时，返回false；
     *
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
     * 检查当前时间是否为工作日（周一至周五）
     *
     * @return
     */
    public static boolean checkCurrentDayIsWorkDay(Date date) {
        boolean flag = false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int weeks = cal.get(Calendar.DAY_OF_WEEK);
        if (weeks >= 2 && weeks <= 6) {
            flag = true;
        }
        return flag;
    }


}


