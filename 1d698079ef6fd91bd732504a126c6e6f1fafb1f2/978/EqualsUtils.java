package com.hlj.proj.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * @author HealerJean
 * @ClassName EqualsUtils
 * @date 2019-08-04  19:47.
 * @Description
 */
public class EqualsUtils {




    /**
     * @param all  全部相等true  有一个相等false
     */
    public final static boolean equals(boolean all, String str, String... strs) {
        if (str != null) {
            if(all){
                for (String value : strs) {
                    if (!StringUtils.equals(value, str)) {
                        return false;
                    }
                }
                return true ;

            }else {
                for (String value : strs) {
                    if (StringUtils.equals(value, str)) {
                        return true;
                    }
                }
                return false ;
            }

        }
        return false;
    }



    /**
     * @param all  全部相等true  有一个相等false
     */
    public final static boolean equals(boolean all, BigDecimal decimal, BigDecimal... decimals) {
        if (decimal != null) {
            if(all){
                for (BigDecimal value : decimals) {
                    if (decimal.compareTo(value) != 0) {
                        return false;
                    }
                }
                return true ;

            }else {
                for (BigDecimal value : decimals) {
                    if (decimal.compareTo(value) == 0) {
                        return true;
                    }
                }
                return false ;
            }

        }
        return false;

    }

    /**
     * @param all  全部相等true  有一个相等false
     */
    public final static boolean equals(boolean all,Integer integer, Integer... integers) {
        if (integer != null) {
            if(all){
                for (Integer value : integers) {
                    if (integer.compareTo(value) != 0) {
                        return false;
                    }
                }
                return true ;

            }else {
                for (Integer value : integers) {
                    if (integer.compareTo(value) == 0) {
                        return true;
                    }
                }
                return false ;
            }

        }
        return false;
    }

    /**
     * @param all  全部相等true  有一个相等false
     */
    public final static boolean equals(boolean all,Long l, Long... longs) {
        if (l != null) {
            if(all){
                for (Long value : longs) {
                    if (l.compareTo(value) != 0) {
                        return false;
                    }
                }
                return true ;

            }else {
                for (Long value : longs) {
                    if (l.compareTo(value) == 0) {
                        return true;
                    }
                }
                return false ;
            }

        }
        return false;
    }


    /**
     * 比较大小
     *
     * @param classType
     * @param value
     * @param otherVale * 1、如果有一个参数为空，则返回 -2 ,没有匹配的类型返回2
     *                  * 2、大小 返回 1、  = 返回0、 小于 -1
     */
    public static Integer compare(Class classType, Object value, Object otherVale) {
        if (value == null || otherVale == null) {
            return -2;
        }
        if (classType.equals(Long.class)) {

            return Long.valueOf(value.toString()).compareTo(Long.valueOf(otherVale.toString()));

        } else if (classType.equals(BigDecimal.class)) {

            return new BigDecimal(value.toString()).compareTo(new BigDecimal(otherVale.toString()));

        } else if (classType.equals(Integer.class)) {

            return Integer.valueOf(value.toString()).compareTo(Integer.valueOf(otherVale.toString()));
        }

        return 2;
    }

}
