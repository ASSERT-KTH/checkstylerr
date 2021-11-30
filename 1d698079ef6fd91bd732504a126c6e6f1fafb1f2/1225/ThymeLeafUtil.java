package com.fintech.scf.utils;

import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ThymeLeafUtil
 * @date 2019/5/16  12:00.
 * @Description
 */
@Slf4j
public class ThymeLeafUtil {


    /**
     * 模板目录 /resource/template/……
     * @param templateName 模板名字 比如 example.html -> examle
     * @param params
     * @return
     */
    public static String htmlTemplate(String templateName, Map params){

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        //模板所在目录,默认resource
        resolver.setPrefix("/thymeleaf/");
        //模板文件后缀
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("utf-8");

        TemplateEngine engine=new TemplateEngine();
        engine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariables(params);

        return    engine.process(templateName,context) ;

    }

    /**
     * @param html 字符串的HTML模板Content 举例 ：<p th:text='${name}'></p>
     * @param params map
     */
    public static String stringTemplate(String html, Map params) {

        SpringTemplateEngine engine = new SpringTemplateEngine();

        StringTemplateResolver resolver = new StringTemplateResolver();
        engine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariables(params);

        return   engine.process(html,context);
    }

    public static void main(String[] args) {
        Map map = new HashMap();
        map.put("debit.loan_contract_no","HealeJean");
        String   content = ThymeLeafUtil.htmlTemplate("sendMailTest",map);
        System.out.println(content);
    }



}
