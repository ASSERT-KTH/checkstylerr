package com.hlj.util.Z012日期.D01Local;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * @ClassName LocalDateUtils
 * @Author TD
 * @Date 2019/4/19 17:24
 * @Description 时间工具
 */
public class LocalDateUtils {

    public static final String YYYYMMdd = "yyyyMMdd";
    public static final String YYYY_MM_dd = "yyyy-MM-dd";
    public static final String YYYY_MM_dd_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";


    public static LocalDate toLocalDate(String dateStr, String format){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        LocalDate parse = LocalDate.parse(dateStr , df);
        return  parse;
    }

    public static String toDateString(LocalDate localDate, String format){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return df.format(localDate);
    }

    public static LocalDateTime toLocalDateTime(String dateStr, String format){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        LocalDateTime parse = LocalDateTime.parse(dateStr , df);
        return  parse;
    }
    public static String  toDateString(LocalDateTime localDateTime, String format){
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return  localDateTime.format(df);
        //下面这个也可以
        // return df.format(localDateTime);
    }



    public static LocalDateTime localDateToLocalDateTime (LocalDate localDate){
        String dateStr = LocalDateUtils.toDateString(localDate, LocalDateUtils.YYYY_MM_dd);
        LocalDateTime localDateTime = LocalDateUtils.toLocalDateTime(dateStr+" 00:00:00", LocalDateUtils.YYYY_MM_dd_HH_mm_ss);
        return localDateTime ;
    }

    public static Long toTimeInSecond(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond() ;
    }

    public static Long toTimeInMills(LocalDateTime localDateTime){
        Timestamp timestamp= Timestamp.valueOf(localDateTime);
        return timestamp.getTime();
    }



    public static Date toDate(LocalDateTime localDateTime){
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return  Date.from(instant);
    }

    public static LocalDateTime toLocalDateTime(Date date){
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }


    public static Date toDate(LocalDate localDate){
        Instant instant = localDate.atStartOfDay( ZoneId.systemDefault()).toInstant();
        return  Date.from(instant);
    }
    public static LocalDate toLocalDate(Date date){
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

}
