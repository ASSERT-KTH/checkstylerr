package com.java110.app;

import com.java110.service.init.ServiceStartInit;
import com.java110.front.core.VueComponentTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;


/**
 * spring boot 初始化启动类
 *
 * @version v0.1
 * @auther com.java110.wuxw
 * @mail 928255095@qq.com
 * @date 2016年8月6日
 * @tag
 */
@SpringBootApplication(scanBasePackages = {"com.java110.front.aop",
        "com.java110.service.configuration",
        "com.java110.front.filter",
        "com.java110.service.init",
        "com.java110.app",
        "com.java110.core",
        "com.java110.config.properties.code",
        "com.java110.cache"
})
@EnableDiscoveryClient
//@EnableConfigurationProperties(EventProperties.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
public class AppFrontServiceApplicationStart {

    private static Logger logger = LoggerFactory.getLogger(AppFrontServiceApplicationStart.class);


    /**
     * 实例化RestTemplate，通过@LoadBalanced注解开启均衡负载能力.
     *
     * @return restTemplate
     */
    @Bean
    //@LoadBalanced
    public RestTemplate restTemplate() {
        StringHttpMessageConverter m = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        RestTemplate restTemplate = new RestTemplateBuilder().additionalMessageConverters(m).build();
        return restTemplate;
    }


    public static void main(String[] args) throws Exception {
        try {
            ApplicationContext context = SpringApplication.run(AppFrontServiceApplicationStart.class, args);
            ServiceStartInit.initSystemConfig(context);
            VueComponentTemplate.initComponent(VueComponentTemplate.DEFAULT_COMPONENT_PACKAGE_PATH);
        }catch (Throwable e){
            logger.error("系统启动失败",e);
        }
    }
}