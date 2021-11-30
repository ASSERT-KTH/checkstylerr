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
public enum SdkLogicCodeEnum {

    正常(200,"访问正常"),
    超时(4081,"超时"),
    系统内部错误(500,"系统内部错误"),
    请求未发出(5001,"请求未发出"),
    UNKNOWN(999,"未知错误")
    ;

    public int code;

    public String desc;

    SdkLogicCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static String getDesc(int code){
        for(SdkLogicCodeEnum desc: SdkLogicCodeEnum.values()){
            if(desc.code == code){
                return desc.desc;
            }
        }
        return SdkLogicCodeEnum.UNKNOWN.desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
