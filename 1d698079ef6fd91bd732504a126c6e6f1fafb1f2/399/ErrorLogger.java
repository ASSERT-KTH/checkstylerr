package com.hlj.moudle.design.D06访问数据结构.D14ChanOfResponsibility责任链模式;

/**
 * @author HealerJean
 * @ClassName ErrorLogger
 * @date 2019/8/16  15:56.
 * @Description
 */
public class ErrorLogger extends AbstractLogger {

    public ErrorLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("Error : " + message);
    }
}
