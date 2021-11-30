package com.duodian.youhui.enums.wechat;


import org.apache.commons.lang3.StringUtils;

/**
 * @Desc: 微信支付错误代码
 * https://pay.weixin.qq.com/wiki/doc/api/tools/mch_pay.php?chapter=14_2
 * @Date:  2018/6/1 上午10:09.
 */

public enum EnumPayErrorType {

    NO_AUTH("NO_AUTH","没有该接口权限"),
    AMOUNT_LIMIT("AMOUNT_LIMIT","金额超限"),
    PARAM_ERROR("PARAM_ERROR","参数错误"),
    OPENID_ERROR("OPENID_ERROR","Openid错误"),
    SEND_FAILED("SEND_FAILED","付款错误"),
    NOTENOUGH("NOTENOUGH","余额不足"),
    SYSTEMERROR("SYSTEMERROR","系统繁忙，请稍后再试。"),
    NAME_MISMATCH("NAME_MISMATCH","姓名校验出错"),
    SIGN_ERROR("SIGN_ERROR","签名错误"),
    XML_ERROR("XML_ERROR","Post内容出错"),
    FATAL_ERROR("FATAL_ERROR","两次请求参数不一致"),
    FREQ_LIMIT("FREQ_LIMIT","超过频率限制，请稍后再试。"),
    MONEY_LIMIT("MONEY_LIMIT","已经达到今日付款总额上限/已达到付款给此用户额度上限"),
    CA_ERROR("CA_ERROR","商户API证书校验出错"),
    V2_ACCOUNT_SIMPLE_BAN("V2_ACCOUNT_SIMPLE_BAN","无法给非实名用户付款"),
    PARAM_IS_NOT_UTF8("PARAM_IS_NOT_UTF8","该用户今日付款次数超过限制,如有需要请登录微信支付商户平台更改API安全配置"),

    NOT_FOUND("NOT_FOUND","指定单号数据不存在"),


    未知("99","未知错误")
    ;

    public String code;
    public String des;

    EnumPayErrorType(String code, String des) {
        this.code = code;
        this.des = des;
    }


    public String getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }

    public static boolean checkExist(String code){
        if(StringUtils.isBlank(code)) return false;
        for (EnumPayErrorType value : EnumPayErrorType.values()){
            if (value.code.equals(code)){
                return true;
            }
        }
        return false;
    }


    public static EnumPayErrorType getEnumPayErrorType(String code){
        if(StringUtils.isBlank(code)) return EnumPayErrorType.未知;
        for (EnumPayErrorType value : EnumPayErrorType.values()){
            if (value.code.equals(code)){
                return value;
            }
        }
        return EnumPayErrorType.未知;
    }

    public static String getDes(String code){
        if(StringUtils.isBlank(code)) return EnumPayErrorType.未知.des;
        for (EnumPayErrorType value : EnumPayErrorType.values()){
            if (value.code.equals(code)){
                return value.des;
            }
        }
        return EnumPayErrorType.未知.des;
    }
}
