package com.hlj.moudle.design.D06访问数据结构.D14ChanOfResponsibility责任链模式;

/**
 * @author HealerJean
 * @ClassName InfoLogger
 * @date 2019/8/16  15:55.
 * @Description
 */
public class InfoLogger extends AbstractLogger {

    public InfoLogger(int level){
        this.level = level;
    }

    @Override
    protected void write(String message) {
        System.out.println("info : " + message);
    }
}
