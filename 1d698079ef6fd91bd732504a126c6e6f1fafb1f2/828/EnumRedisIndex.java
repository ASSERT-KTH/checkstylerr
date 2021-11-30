package com.hlj.netty.websocket.redis;


/**
 * 类名称：EnumRedisIndex
 * 类描述：redis枚举类型
 * 创建人：qingxu
 * 修改人：
 * 修改时间：2015/12/19 17:47
 * 修改备注：
 *
 * @version 1.0.0
 */
public enum EnumRedisIndex {

    普通临时变量(3,"普通临时变量"),
    通用状态标记(5,"通用状态标记"),
    广告主广告限制(100,"广告主广告限制"),
    用户广告黑名单(101,"用户广告黑名单"),
    流量主PV统计(102,"流量主PV统计"),

    未知(99,"未知")
    ;

    public Integer index;
    public String des;

    EnumRedisIndex(Integer index, String des) {
        this.index = index;
        this.des = des;
    }


    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getDes() {
        return des;
    }

    public static boolean checkExist(Integer index){
        if(index == null) return true;
        for (EnumRedisIndex value : EnumRedisIndex.values()){
            if (value.index.compareTo(index) == 0){
                return true;
            }
        }
        return false;
    }



    public static EnumRedisIndex getRedisIndex(Integer index){
        if(index == null) return EnumRedisIndex.未知;
        for (EnumRedisIndex value : EnumRedisIndex.values()){
            if (value.index.compareTo(index) == 0){
                return value;
            }
        }
        return EnumRedisIndex.未知;
    }

    public static String getDes(Integer index){
        if(index == null) return EnumRedisIndex.未知.des;
        for (EnumRedisIndex value : EnumRedisIndex.values()){
            if (value.index.compareTo(index) == 0){
                return value.des;
            }
        }
        return EnumRedisIndex.未知.des;
    }
}
