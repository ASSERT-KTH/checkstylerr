package com.duodian.youhui.enums.http;

/**
 * 类名称：SdkHttpCodeEnum
 * 类描述：http错误状态枚举值
 * 创建人：qingxu
 * 修改人：
 * 修改时间：2015/11/5 11:27
 * 修改备注：
 *
 * @version 1.0.0
 */
public enum SdkHttpCodeEnum {

    全部(100,SdkDutyToEnum.无责任方.dutyTo,"全部"),
    正常(200,SdkDutyToEnum.无责任方.dutyTo,"访问正常"),
    超时(4081,SdkDutyToEnum.客户超时.dutyTo,"超时"),
    逻辑错误(3001,SdkDutyToEnum.自己问题.dutyTo,"逻辑错误"),
    系统内部错误(500,SdkDutyToEnum.客户报错.dutyTo,"系统内部错误"),
    请求未发出(5001,SdkDutyToEnum.自己问题.dutyTo,"请求未发出"),
    UNKNOWN(999,SdkDutyToEnum.自己问题.dutyTo,"未知错误")
    ;

    public int code;

    public String desc;

    public int dutyTo;  // 0 无责任方 1 客户方超时 2 客户方系统内部错误 3我方综合问题

    SdkHttpCodeEnum(int code, int dutyTo, String desc) {
        this.code = code;
        this.desc = desc;
        this.dutyTo = dutyTo;
    }

    public static String getDesc(Integer code){
        if(code == null) return SdkHttpCodeEnum.UNKNOWN.desc;
        for(SdkHttpCodeEnum desc: SdkHttpCodeEnum.values()){
            if(desc.code == code.intValue()){
                return desc.desc;
            }
        }
        return SdkHttpCodeEnum.UNKNOWN.desc;
    }

    public static SdkHttpCodeEnum getHttpCodeEnum(Integer code){
        if(code == null) return SdkHttpCodeEnum.UNKNOWN;
        for (SdkHttpCodeEnum value : SdkHttpCodeEnum.values()){
            if (value.code == code.intValue()){
                return value;
            }
        }
        return SdkHttpCodeEnum.UNKNOWN;
    }

    public static int getDuty(Integer code){
        if(code == null) return SdkHttpCodeEnum.UNKNOWN.dutyTo;
        for (SdkHttpCodeEnum value : SdkHttpCodeEnum.values()){
            if (value.code == code.intValue()){
                return value.dutyTo;
            }
        }
        return SdkHttpCodeEnum.UNKNOWN.dutyTo;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getDutyTo() {
        return dutyTo;
    }
}
