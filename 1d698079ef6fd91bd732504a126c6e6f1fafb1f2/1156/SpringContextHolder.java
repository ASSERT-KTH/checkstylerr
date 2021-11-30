package com.hlj.proj.service.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;


@Order
@Service("scfSpringContextHolder")
public class SpringContextHolder implements ApplicationContextAware, BeanFactoryPostProcessor, Ordered {


    private static ApplicationContext _applicationContext;
    private static ConfigurableListableBeanFactory _beanFactory;

    public static WebApplicationContext getWebApplicationContext() {
        return (WebApplicationContext) _applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return _applicationContext;
    }

    public static Object getBean(String beanName) {
        return _beanFactory.containsBean(beanName) ? _beanFactory.getBean(beanName) : null;
    }

    public static <T> T getBean(Class<T> clasz) {
        return _beanFactory.getBean(clasz);
    }

    public static <T> T getBean(String beanName, Class<T> clasz) {
        return _beanFactory.containsBean(beanName) ? _beanFactory.getBean(beanName, clasz) : null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        _applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        _beanFactory = beanFactory;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
