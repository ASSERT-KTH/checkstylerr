package com.ctrip.framework.apollo.spring.annotation;

import com.ctrip.framework.apollo.build.ApolloInjector;
import com.ctrip.framework.apollo.spring.config.AutoUpdateConfigChangeListener;
import com.ctrip.framework.apollo.spring.property.PlaceholderHelper;
import com.ctrip.framework.apollo.spring.property.SpringValue;
import com.ctrip.framework.apollo.util.ConfigUtil;
import com.ctrip.framework.foundation.internals.Utils;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;

/**
 * Create by zhangzheng on 2018/2/6
 */
public class ApolloJSONValueProcessor extends ApolloProcessor implements EnvironmentAware,BeanFactoryAware {

  private Logger logger = LoggerFactory.getLogger(ApolloJSONValueProcessor.class);

  private static Gson gson = new Gson();


  private Environment environment;
  private final ConfigUtil configUtil;
  private final PlaceholderHelper placeholderHelper;
  private ConfigurableBeanFactory beanFactory;

  public ApolloJSONValueProcessor() {
    configUtil = ApolloInjector.getInstance(ConfigUtil.class);
    placeholderHelper = ApolloInjector.getInstance(PlaceholderHelper.class);
  }

  @Override
  protected void processField(Object bean,String beanName, Field field) {
    ApolloJSONValue apolloJSONValue = AnnotationUtils.getAnnotation(field, ApolloJSONValue.class);
    if (apolloJSONValue == null) {
      return;
    }
    try {
      String placeHolder = apolloJSONValue.value();
      String propertyValue = beanFactory.resolveEmbeddedValue(placeHolder);
      if(!Utils.isBlank(propertyValue)){
        boolean accessible = field.isAccessible();
        field.setAccessible(true);
        field.set(bean, gson.fromJson(propertyValue, field.getGenericType()));
        field.setAccessible(accessible);
      }
      if(configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()){
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
        for(String key:keys){
          SpringValue springValue = new SpringValue(key, placeHolder, bean, beanName, field, true);
          AutoUpdateConfigChangeListener.monitor.put(key, springValue);
          logger.debug("Monitoring ", springValue);
        }
      }
    } catch (Exception e) {
      logger.error("set json value exception", e);
    }

  }

  @Override
  protected void processMethod(Object bean, String beanName, Method method) {

    ApolloJSONValue apolloJSONValue = AnnotationUtils.getAnnotation(method, ApolloJSONValue.class);
    if (apolloJSONValue == null) {
      return;
    }
    try {
      String placeHolder = apolloJSONValue.value();
      String propertyValue = beanFactory.resolveEmbeddedValue(placeHolder);
      if(!Utils.isBlank(propertyValue)){
        boolean accessible = method.isAccessible();
        method.setAccessible(true);
        Type[] types = method.getGenericParameterTypes();
        Preconditions.checkArgument(types.length == 1, "Ignore @Value setter {}.{}, expecting 1 parameter, actual {} parameters",
            bean.getClass().getName(), method.getName(), method.getParameterTypes().length);
        method.invoke(bean, gson.fromJson(propertyValue, types[0]));
        method.setAccessible(accessible);
      }
      if(configUtil.isAutoUpdateInjectedSpringPropertiesEnabled()){
        Set<String> keys = placeholderHelper.extractPlaceholderKeys(placeHolder);
        for(String key:keys){
          SpringValue springValue = new SpringValue(key, apolloJSONValue.value(), bean, beanName, method, true);
          AutoUpdateConfigChangeListener.monitor.put(key, springValue);
          logger.debug("Monitoring ", springValue);
        }
      }
    } catch (Exception e) {
      logger.error("set json value exception", e);
    }
  }



  @Override
  public void setEnvironment(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = (ConfigurableBeanFactory) beanFactory;
  }
}
