package com.duodian.youhui.enums;


import java.util.Arrays;
import java.util.List;

/**
 * @Desc: 是否有效
 * @Date:  2018/6/1 上午10:09.
 */

public enum EnumDelete {

    已删除(0,"已删除"),
    可用(1,"可用"),



    未知(99,"未知")
    ;

    public Integer status;
    public String des;

    EnumDelete(Integer status, String des) {
        this.status = status;
        this.des = des;
    }


    public Integer getStatus() {
        return status;
    }

    public String getDes() {
        return des;
    }

    public static boolean checkExist(Integer source){
        for (EnumDelete value : EnumDelete.values()){
            if (value.status == source){
                return true;
            }
        }
        return false;
    }



    public static EnumDelete getStatus(Integer status){
        for (EnumDelete value : EnumDelete.values()){
            if (value.status == status){
                return value;
            }
        }
        return EnumDelete.未知;
    }

    public static String getDes(Integer status){
        for (EnumDelete value : EnumDelete.values()){
            if (value.status == status){
                return value.des;
            }
        }
        return EnumDelete.未知.des;
    }

    public static List<EnumDelete> getTypeList(){
        return Arrays.asList(values());
    }
}
