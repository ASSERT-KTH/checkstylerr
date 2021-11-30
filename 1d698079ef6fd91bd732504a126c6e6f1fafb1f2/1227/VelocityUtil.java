package com.fintech.scf.utils;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import java.io.StringWriter;
import java.util.Map;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName VelocityUtil
 * @date 2019/5/16  15:19.
 * @Description Velocity 工具类
 */
public class VelocityUtil {

    /**
     * 根据vm模板获取内容 目录 resurce/vm/……
     * @param templateName 模板名字 example.vm -> example
     * @param params
     * @return
     */
    public static String vmTemplate(String templateName, Map params) {
        // 初始化模板引擎
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        engine.init();
        //模板所在目录
        String VM_PATH = "vm/"+templateName+".vm";

        // 获取模板文件
        Template template = engine.getTemplate(VM_PATH,"utf-8");

        // 设置变量，velocityContext是一个类似map的结构
        VelocityContext context = new VelocityContext(params);

        // 输出渲染后的结果
        StringWriter sw = new StringWriter();
        template.merge(context, sw);
        System.out.println(sw.toString());
        return   sw.toString() ;
    }



    /**
     * 根据字符串获取 模板内容
     * @param content
     * @param params
     * @return
     */
    public static String stringTemplate(String content, Map params) {

        // 初始化并取得Velocity引擎
        VelocityEngine engine = new VelocityEngine();
        engine.init();

        // 取得velocity的上下文context
        VelocityContext context = new VelocityContext(params);

        StringWriter sw = new StringWriter();
        // 转换输出
        engine.evaluate(context, sw, "", content); // 关键方法
        return sw.toString() ;
    }
}
