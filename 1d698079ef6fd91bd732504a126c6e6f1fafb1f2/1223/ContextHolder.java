package com.duodian.admore.zhaobutong.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author fengchuanbo
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ContextHolder {

    /** 将openId保存到上下文 */
    private final static ThreadLocal<String> oidContainer = new ThreadLocal<>();
    private final static ThreadLocal<Long> userIdCcontainer = new ThreadLocal<>();

    /**
     * 把userId保存到上下文
     * @param userId
     */
    public static void setUserId(Long userId){
        userIdCcontainer.set(userId);
    }

    /**
     * 把openId保存到上下文
     * @param openId
     */
    public static void setOid(String openId){
        oidContainer.set(openId);
    }

    public static void setUserIdAndOid(Long userId, String openId){
        setUserId(userId);
        setOid(openId);
    }

    /**
     * 获取用户ID
     * @return
     */
    public static Long getUserId(){
        return userIdCcontainer.get();
    }

    /**
     * 获取用户oid
     * @return
     */
    public static String getOid(){
        return oidContainer.get();
    }

    /**
     * 清空上下文
     */
    public static void clear(){
        oidContainer.remove();
        userIdCcontainer.remove();
    }
}
