package com.duodian.youhui.enums.http;

/**
 * 类名称：SdkDutyToEnum
 * 类描述：责任方枚举值
 * 创建人：qingxu
 * 修改人：
 * 修改时间：2015/11/5 11:27
 * 修改备注：
 *
 * @version 1.0.0
 */
public enum SdkDutyToEnum {

    无责任方(1,"无责任方"),
    客户超时(2,"客户超时"),
    客户报错(3,"客户报错"),
    自己问题(4,"自己问题"),
    UNKNOWN(9,"未知错误")
    ;

    public String desc;

    public int dutyTo;  // 1 无责任方 2 客户方超时 3 客户方系统内部错误 4我方综合问题

    SdkDutyToEnum(int dutyTo, String desc) {
        this.desc = desc;
        this.dutyTo = dutyTo;
    }

    public static String getDesc(Integer dutyTo){
        if(dutyTo == null) return SdkDutyToEnum.UNKNOWN.desc;
        for(SdkDutyToEnum desc: SdkDutyToEnum.values()){
            if(desc.dutyTo == dutyTo.intValue()){
                return desc.desc;
            }
        }
        return SdkDutyToEnum.UNKNOWN.desc;
    }

    public static SdkDutyToEnum getDutyToEnum(Integer dutyTo){
        if(dutyTo == null) return SdkDutyToEnum.UNKNOWN;
        for (SdkDutyToEnum value : SdkDutyToEnum.values()){
            if (value.dutyTo == dutyTo.intValue()){
                return value;
            }
        }
        return SdkDutyToEnum.UNKNOWN;
    }

    public String getDesc() {
        return desc;
    }

    public int getDutyTo() {
        return dutyTo;
    }
}
