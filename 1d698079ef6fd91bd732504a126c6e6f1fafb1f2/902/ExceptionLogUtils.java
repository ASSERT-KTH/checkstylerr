package com.duodian.youhui.admin.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Desc: 异常打印日志 ，提供给打印非正常异常
 * @Author HealerJean
 * @Date 2018/8/21  下午6:41.
 */
@Slf4j
public class ExceptionLogUtils {



    public static void log(Throwable e,Class c){
        Logger logger = LoggerFactory.getLogger(c);
//      logger.error("错误堆栈", e);

        StackTraceElement s= e.getStackTrace()[0];//数组长度为 1
        logger.error("\n\n-----------------"+
                    "\n报错文件名:"+s.getFileName()+
                    "\n报错的类："+s.getClassName()+
                    "\n报错方法：："+s.getMethodName()+
                    "\n报错的行："+ s.getLineNumber()+
                    "\n报错的message："+ e.getMessage()+
                    "\n错误堆栈：\n"+getStackTrace(e)+
                    "\n------------------\n\n");
      }




    public static void logInfo(String msg,Class c){
        Logger logger = LoggerFactory.getLogger(c);
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();//数组长度为 3
        logger.info("\n\n**************"+
                "\n打印文件名："+stacks[2].getFileName() +
                "\n打印类名："+  stacks[2].getClassName() +
                "\n方法名：" +  stacks[2].getMethodName() +
                "\n行号："  +  stacks[2].getLineNumber() +
                "\n打印内容:"+msg+
                "\n**************\n\n");
    }

    /**
     * 打印我们自己认为错误的信息
     * @param msg
     * @param c
     */
    public static void logError(String msg,Class c){
        Logger logger = LoggerFactory.getLogger(c);
        StackTraceElement[] stacks = Thread.currentThread().getStackTrace();//数组长度为 3
        logger.info("\n\n**************"+
                "\n打印文件名："+stacks[2].getFileName() +
                "\n打印类名："+  stacks[2].getClassName() +
                "\n方法名：" +  stacks[2].getMethodName() +
                "\n行号："  +  stacks[2].getLineNumber() +
                "\n打印内容:"+msg+
                "\n**************\n\n");
    }



    /**
     * 获取堆栈信息
     * @param throwable
     * @return
     */
    public static String getStackTrace(Throwable throwable){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try
        {
            throwable.printStackTrace(pw);
            return sw.toString();
        } finally
        {
            pw.close();
        }
    }


    /**
     * 获取异常消息
     * @param e
     * @param length，小于等于 0 时，不进行长度限制
     * @return
     */
    public static String getExceptionMessage(Exception e, Integer length) {
        String msg = null;
        if (e instanceof NullPointerException) {
            msg = "java.lang.NullPointerException";
        } else {
            msg = e.getMessage();
        }
        if (length == null || length <= 0) {
            return msg;
        }
        if (msg.length() > length) {
            msg = msg.substring(0, length);
        }
        return msg;
    }



    /**
     * 将CheckedException转换为UncheckedException
     * @param e
     * @return
     */
    public static RuntimeException toUncheckedException(Exception e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        } else {
            return new RuntimeException(e);
        }
    }



}
