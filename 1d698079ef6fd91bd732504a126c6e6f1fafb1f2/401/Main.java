package com.hlj.moudle.design.D06访问数据结构.D14ChanOfResponsibility责任链模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/16  15:57.
 * @Description
 */
public class Main {

    private static AbstractLogger getChainOfLoggers(){
        AbstractLogger errorLogger = new ErrorLogger(AbstractLogger.ERROR);
        AbstractLogger infoLogger =  new InfoLogger(AbstractLogger.INFO);
        errorLogger.setNextLogger(infoLogger);

        AbstractLogger debugLogger = new DebugLogger(AbstractLogger.DEBUG);
        infoLogger.setNextLogger(debugLogger);

        return errorLogger;
    }

    public static void main(String[] args) {
        AbstractLogger loggerChain = getChainOfLoggers();

        loggerChain.log(AbstractLogger.ERROR, " error message");
        // Error :  error message
        // info :  error message
        // Debug :  error message


        loggerChain.log(AbstractLogger.INFO, "info message");
        // info : info message
        // info :  debug message

        loggerChain.log(AbstractLogger.DEBUG, " debug message ");
        // Debug :  debug message


    }
}
