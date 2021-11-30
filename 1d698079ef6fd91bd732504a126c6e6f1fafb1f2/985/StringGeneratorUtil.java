package com.duodian.youhui.admin.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Desc:  字符串工具类
 * @Date:  2018/9/11 上午11:00.
 */
public class StringGeneratorUtil {


    private static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String letterChar = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String digitChar = "0123456789";


    private StringGeneratorUtil(){
    }

    /**
     * 唯一uuid
     * @return
     */
    public static String generate(){
        return UUID.randomUUID().toString().replace("-","");
    }

    /**
     * 随机字符数字大小写混合字符串，不保证唯一
     * @param count
     * @return
     */
    public static String generate(int count){
        return RandomStringUtils.random(count,true,true);
    }



    /**
     * 随机字符大小写混合字符串，不保证唯一
     * @param count
     * @return
     */
    public static String generateAlpha(int count){
        return RandomStringUtils.randomAlphabetic(count);
    }


    /**
     * 可用于短信验证码
     * 返回一个定长的随机字符串 (只包含数字)
     * @param length  随机字符串长度
     * @return 随机字符串
     */
    public static String generateDigitString(int length){
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i=0;i<length;i++){
            sb.append(digitChar.charAt(random.nextInt(digitChar.length())));
        }
        return sb.toString();
    }


    /**
     * 返回一个6位数字验证码
     */
    public static String genrateVerifyCode6() {
        Random random = new Random();
        int x = random.nextInt(899999);
        return String.valueOf(x + 100000);
    }
    /**
     * 返回一个4位数字验证码
     */
    public static String genrateVerifyCode4() {
        Random random = new Random();
        int x = random.nextInt(8999);
        return String.valueOf(x + 1000);
    }


    /**
     * 生成一个位于最小值与最大值之间的整数
     * @param min 最小值
     * @param max 最大值
     * @return 数
     */
    public static int generateIntBetweenMinMax(int min,int max){
        Random random = new Random();
        if(min > max){
            int tmp = 0;
            tmp = min;
            min = max;
            max = tmp;
        }
        return random.nextInt(max - min) + min;
    }

    /**
     * 返回一个定长的随机字符串(只包含大小写字母、数字)
     * @param length  随机字符串长度
     * @return 随机字符串
     */
    public static String generateString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(allChar.length())));
        }
        return sb.toString();
    }

    /**
     * 生成一个位于两数值之间的随机长度字符串
     * @param minLen    最小长度值
     * @param maxLen    最大长度值
     * @return 字符串
     */
    public static String generateString(int minLen,int maxLen){
        Random random = new Random();
        if(minLen > maxLen){
            int tmp = 0;
            tmp = minLen;
            minLen = maxLen;
            maxLen = tmp;
        }
        return generateString(random.nextInt(maxLen - minLen) + minLen);
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     * @param length  随机字符串长度
     * @return 随机字符串
     */
    public static String generateMixString(int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(allChar.charAt(random.nextInt(letterChar.length())));
        }
        return sb.toString();
    }




    private static boolean isMatch(String password, String pattern) {
        // 创建 Pattern 对象
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(password);
        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }
    /**
     * 判断密码强度
     *
     * @param password
     * @return
     */
    public static int passwordStrength(String password) {
        if (password.length() < 6) {
            return 1;
        } else {
            String strongPattern = "^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)(?![a-zA-z\\d]+$)(?![a-zA-z!@#$%^&*]+$)(?![\\d!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$";
            String inPattern = "^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$";
            String weakPattern = "^(?:\\d+|[a-zA-Z]+|[!@#$%^&*]+)$";
            if (isMatch(password, strongPattern)) {
                //强
                return 3;
            } else if (isMatch(password, inPattern)) {
                //中
                return 2;
            } else {
                //弱
                return 1;
            }
        }
    }

    /**
     *         String requestHeader = req.getHeader("user-agent");
     * @param userAgent
     * @return
     */
    //判断是否为手机
    public static Boolean checkIsMoblie(String userAgent) {
        boolean isMoblie = false;
        String[] mobileAgents = { "iphone", "android","ipad", "phone", "mobile", "wap", "netfront", "java", "opera mobi",
                "opera mini", "ucweb", "windows ce", "symbian", "series", "webos", "sony", "blackberry", "dopod",
                "nokia", "samsung", "palmsource", "xda", "pieplus", "meizu", "midp", "cldc", "motorola", "foma",
                "docomo", "up.browser", "up.link", "blazer", "helio", "hosin", "huawei", "novarra", "coolpad", "webos",
                "techfaith", "palmsource", "alcatel", "amoi", "ktouch", "nexian", "ericsson", "philips", "sagem",
                "wellcom", "bunjalloo", "maui", "smartphone", "iemobile", "spice", "bird", "zte-", "longcos",
                "pantech", "gionee", "portalmmm", "jig browser", "hiptop", "benq", "haier", "^lct", "320x320",
                "240x320", "176x220", "w3c ", "acs-", "alav", "alca", "amoi", "audi", "avan", "benq", "bird", "blac",
                "blaz", "brew", "cell", "cldc", "cmd-", "dang", "doco", "eric", "hipt", "inno", "ipaq", "java", "jigs",
                "kddi", "keji", "leno", "lg-c", "lg-d", "lg-g", "lge-", "maui", "maxo", "midp", "mits", "mmef", "mobi",
                "mot-", "moto", "mwbp", "nec-", "newt", "noki", "oper", "palm", "pana", "pant", "phil", "play", "port",
                "prox", "qwap", "sage", "sams", "sany", "sch-", "sec-", "send", "seri", "sgh-", "shar", "sie-", "siem",
                "smal", "smar", "sony", "sph-", "symb", "t-mo", "teli", "tim-", "tosh", "tsm-", "upg1", "upsi", "vk-v",
                "voda", "wap-", "wapa", "wapi", "wapp", "wapr", "webc", "winw", "winw", "xda", "xda-",
                "Googlebot-Mobile" };
        if (userAgent != null) {
            userAgent = userAgent.toLowerCase();
            for (String mobileAgent : mobileAgents) {
                if (userAgent.contains(mobileAgent) && userAgent.indexOf("windows nt") <= 0 && userAgent.indexOf("macintosh") <= 0) {
                    isMoblie = true;
                    break;
                }
            }
        }
        return isMoblie;
    }

    /**
     * 判断是不是电话号码
     *
     * @param str
     * @return 验证通过返回true
     */
    public static boolean isPhone(String str) {
        Pattern p1 = null, p2 = null;
        Matcher m = null;
        boolean b = false;
        p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的
        if (str.length() > 9) {
            m = p1.matcher(str);
            b = m.matches();
        } else {
            m = p2.matcher(str);
            b = m.matches();
        }
        return b;
    }

    /**
     * 判断是不是手机号码
     * @param mobile
     * @return
     */
    public final static boolean checkMobile(String mobile) {
        /**
         * 运营商号段如下：
         * 中国联通号码：130、131、132、145（无线上网卡）、155、156、185（iPhone5上市后开放）、186、176（4G号段）、
         *               175（2015年9月10日正式启用，暂只对北京、上海和广东投放办理）
         * 中国移动号码：134、135、136、137、138、139、147（无线上网卡）、150、151、152、157、158、159、182、183、187、188、178
         * 中国电信号码：133、153、180、181、189、177、173、149 虚拟运营商：170、1718、1719
         * 手机号前3位的数字包括：
         * 1 :1
         * 2 :3,4,5,7,8
         * 3 :0,1,2,3,4,5,6,7,8,9
         * 总结： 目前java手机号码正则表达式有：
         * a :"^1[3|4|5|7|8][0-9]\\d{4,8}$"    一般验证情况下这个就可以了
         * b :"^((13[0-9])|(14[5|7])|(15([0-3]|[5-9]))|(17[013678])|(18[0,5-9]))\\d{8}$"
         * Pattern和Matcher详解（字符串匹配和字节码）http://blog.csdn.net/u010700335/article/details/44616451
         */
        String regex = "^1[0-9][0-9]\\d{4,8}$";
        if (mobile != null && mobile.length() == 11) {
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(mobile);
            boolean isMatch = m.matches();
            if (isMatch) {
                return true;
            }
        }
        return false;
    }



    /**
     * 把原始字符串分割成指定长度的字符串列表
     * @param inputString  原始字符串
     * @param length   指定长度
     * @return
     */
    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    /**
     * 把原始字符串分割成指定长度的字符串列表
     * @param inputString 原始字符串
     * @param length 指定长度
     * @param size  指定列表大小
     * @return
     */
    private static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }
    /**
     * 分割字符串，如果开始位置大于字符串长度，返回空
     * @param str  原始字符串
     * @param f 开始位置
     * @param t  结束位置
     * @return
     */
    private static String substring(String str, int f, int t) {
        if (f > str.length())
            return null;
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }




    public static boolean isEnglish(String str){
        //【除英文和数字外无其他字符(只有英文数字的字符串)】返回true 否则false
        boolean result2 = str.matches("[a-zA-Z0-9]+");
        return result2 ;
    }


    public static  boolean isHaveEnglish(String str ,Integer type){
        switch (type){
            case 1:// 【全为英文】返回true  否则false
                return str.matches("[a-zA-Z]+");
            case 2:// 【除英文和数字外无其他字符(只有英文数字的字符串)】返回true 否则false
                return  str.matches("[a-zA-Z0-9]+");
            case 3: //【含有英文】true
                String regex1 = ".*[a-zA-z].*";
                return str.matches(regex1);
            case 4:// 【含有数字】true
                String regex2 = ".*[0-9].*";
                return str.matches(regex2);
            case 5://  【 判断是否为纯中文，不是返回false】
                String regex3 = "[\\u4e00-\\u9fa5]+";
                return str.matches(regex3);
            case 6: // 【全为数字】返回true
                return  str.matches("[0-9]+");
            default:
                return false ;
        }
    }



    public static boolean equals(String str,String ... values){
          Optional<String> val =  Arrays.stream(values).filter(value-> StringUtils.equals(str,value )).findAny();
          return val.isPresent();
    }



}
