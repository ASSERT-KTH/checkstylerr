package com.hlj.moudle.design.D06访问数据结构.D14ChanOfResponsibility责任链模式;

/**
 * @author HealerJean
 * @ClassName AbstractLogger
 * @date 2019/8/16  15:54.
 * @Description 创建抽象的记录器类。
 */
public abstract  class AbstractLogger {

    /**
     * 级别 error 是最高级别3
     */
    public static int INFO = 1;
    public static int DEBUG = 2;
    public static int ERROR = 3;

    protected int level;

    /**
     * 责任链中的下一个元素
     */
    protected AbstractLogger nextLogger;


    public void setNextLogger(AbstractLogger nextLogger){
        this.nextLogger = nextLogger;
    }

    /**
     * 输入的基本级别如果跟当前级别比较，如果比当前级别大，则肯定打印，
     * 傻瓜 error肯定会打印 info
     */
    public void log(int level, String message){
        if(this.level <= level){
            write(message);
        }
        if(nextLogger !=null){
            nextLogger.log(level, message);
        }
    }

    abstract protected void write(String message);

}
