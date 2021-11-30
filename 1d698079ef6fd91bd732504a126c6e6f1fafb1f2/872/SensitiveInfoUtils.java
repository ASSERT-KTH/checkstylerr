package com.hlj.proj.utils.sensitivity;

import org.apache.commons.lang3.StringUtils;

/**
 * @ClassName SensitiveInfoUtils
 * @Author TD
 * @Date 2019/1/10 17:46
 * @Description 脱敏工具类
 */
public class SensitiveInfoUtils {

    /**
     * [真实姓名] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     */
    public static String realName(final String realName) {
        if (StringUtils.isBlank(realName)) {
            return "";
        }
        return dealString(realName, 1, 0);
    }

    /**
     * [身份证号] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     */
    public static String idCard(final String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        return dealString(idCard, 3, 4);
    }

    /**
     * [手机号] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     */
    public static String mobilePhone(final String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        return dealString(idCard, 3, 4);
    }

    /**
     * [邮箱] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     */
    public static String email(final String email) {
        if (StringUtils.isBlank(email)) {
            return "";
        }
        int index = email.indexOf("@");
        return dealString(email, 3, email.length() - index);
    }

    /**
     * [账号] 显示最后四位，其他隐藏。共计18位或者15位。<例子：*************5762>
     */
    public static String acctNo(final String idCard) {
        if (StringUtils.isBlank(idCard)) {
            return "";
        }
        final String name = StringUtils.left(idCard, 1);
        return StringUtils.rightPad(name, StringUtils.length(idCard), "*");
    }

    /**
     * [密码] 隐藏。<例子：*************>
     */
    public static String password(final String password) {
        if (StringUtils.isBlank(password)) {
            return "";
        }
        return "*";
    }


    private static String dealString(String str, int head_off, int tail_off) {
        int length = str.length();
        StringBuffer sb = new StringBuffer();
        final String head = StringUtils.left(str, head_off);
        String tail = StringUtils.right(str, tail_off);
        sb.append(head);
        int size = length - (head_off + tail_off);
        if (size > 0) {
            while (size > 0) {
                sb.append("*");
                size--;
            }
        }
        sb.append(tail);
        return sb.toString();
    }


    /**
     * 提供给外部进行直接脱敏处理
     * @param type
     * @param value
     * @return
     */
    public static String sensitveObject(SensitiveTypeEnum type, String value) {
        switch (type) {
            case REAL_NAME: {
                return realName(String.valueOf(value));
            }
            case ID_CARD: {
                return idCard(String.valueOf(value));
            }
            case MOBILE_PHONE: {
                return mobilePhone(String.valueOf(value));
            }
            case EMAIL: {
                return email(String.valueOf(value));
            }
            case ACCOUNT_NO: {
                return acctNo(String.valueOf(value));
            }
            case PASSWORD: {
                return password(String.valueOf(value));
            }
            default:
                return String.valueOf(value);
        }

    }


}
