package com.hlj.many.datasourse.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @Description  类似于单例模式数据源
 * @Date   2018/4/24 下午6:10.
 */

public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    private static final ThreadLocal<Stack<String>> stackHolder = new ThreadLocal<>();
    public static List<String> dataSourceIds = new ArrayList<>();

    public static void setDataSource(String dataSourceType) {
        // 保存当前线程上一次的数据源，用于清除时恢复上次的上线文
        if (contextHolder.get() != null){
            Stack<String> stack = stackHolder.get();
            if (stack == null){
                stack = new Stack<>();
                stackHolder.set(stack);
            }
            stack.push(contextHolder.get());
        }
        contextHolder.set(dataSourceType);
    }

    public static String getDataSourceType() {
        return contextHolder.get();
    }

    public static void clearDataSource() {
        // 清除本次的数据源
        contextHolder.remove();
        // 从栈中拿出上一次的数据源
        Stack<String> stack = stackHolder.get();
        if (stack != null){
            if (stack.isEmpty()){
                stackHolder.remove();
            }else{
                String pop = stack.pop();
                contextHolder.set(pop);
                if (stack.isEmpty()){
                    stackHolder.remove();
                }
            }
        }
    }

    /**
     * 判断指定DataSrouce当前是否存在
     * @param dataSourceName
     * @return
     */
    public static boolean containsDataSource(String dataSourceName){
        return dataSourceIds.contains(dataSourceName);
    }

}
