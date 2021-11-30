package com.hlj.many.datasourse.data;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.bind.RelaxedDataBinder;
import org.springframework.boot.bind.RelaxedPropertyResolver;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengchuanbo on 2017/5/8.
 */
@Slf4j
public class DynamicDataSourceRegistrar implements ImportBeanDefinitionRegistrar,EnvironmentAware {

    private ConversionService conversionService = new DefaultConversionService();
    private PropertyValues dataSourcePropertyValues;

    // 如配置文件中未指定数据源类型，使用该默认值
    private static final Object DATASOURCE_TYPE_DEFAULT = "org.apache.tomcat.jdbc.pool.DataSource";

    // 数据源
    private DataSource defaultDataSource;
    private Map<String, DataSource> customDataSources = new HashMap<>();
    private static final String DEFAULT_DATASOURCE_NAME = com.hlj.many.datasourse.data.DataSource.ONE.getName();

    @Override
    public void setEnvironment(Environment environment) {
        initDefaultDataSource(environment);
        initCustomDataSources(environment);
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        // 将主数据源添加到更多数据源中
        targetDataSources.put(DEFAULT_DATASOURCE_NAME, defaultDataSource);
        DynamicDataSourceContextHolder.dataSourceIds.add(DEFAULT_DATASOURCE_NAME);
        // 添加更多数据源
        targetDataSources.putAll(customDataSources);
        for (String key : customDataSources.keySet()) {
            DynamicDataSourceContextHolder.dataSourceIds.add(key);
        }

        // 创建DynamicDataSource
        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(DynamicDataSource.class);
        beanDefinition.setSynthetic(true);
        MutablePropertyValues mpv = beanDefinition.getPropertyValues();
        mpv.addPropertyValue("defaultTargetDataSource", defaultDataSource);
        mpv.addPropertyValue("targetDataSources", targetDataSources);
        registry.registerBeanDefinition(DEFAULT_DATASOURCE_NAME, beanDefinition);

        if (log.isInfoEnabled()) {
            log.info("Dynamic DataSource Registry");
        }
    }

    /**
     * 初始化主数据源
     *
     * @author SHANHY
     * @create 2016年1月24日
     */
    private void initDefaultDataSource(Environment env) {
        // 读取主数据源
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "spring.datasource.");
        Map<String, Object> dsMap = new HashMap<>();
        dsMap.put("type", propertyResolver.getProperty("type"));
        dsMap.put("driver-class-name", propertyResolver.getProperty("driver-class-name"));
        dsMap.put("url", propertyResolver.getProperty("url"));
        dsMap.put("username", propertyResolver.getProperty("username"));
        dsMap.put("password", propertyResolver.getProperty("password"));

        defaultDataSource = buildDataSource(dsMap);
        dataBinder(defaultDataSource, env,"spring.datasource");
    }

    /**
     * 为DataSource绑定更多数据
     * @param dataSource
     * @param env
     */
    private void dataBinder(DataSource dataSource, Environment env, String prefix){
        RelaxedDataBinder dataBinder = new RelaxedDataBinder(dataSource);
        //dataBinder.setValidator(new LocalValidatorFactory().run(this.applicationContext));
        dataBinder.setConversionService(conversionService);
        dataBinder.setIgnoreNestedProperties(false);//false
        dataBinder.setIgnoreInvalidFields(false);//false
        dataBinder.setIgnoreUnknownFields(true);//true
        if(dataSourcePropertyValues == null){
            Map<String, Object> rpr = new RelaxedPropertyResolver(env, prefix).getSubProperties(".");
            Map<String, Object> values = new HashMap<>(rpr);
            // 排除已经设置的属性
            values.remove("type");
            values.remove("driver-class-name");
            values.remove("url");
            values.remove("username");
            values.remove("password");
            dataSourcePropertyValues = new MutablePropertyValues(values);
        }
        dataBinder.bind(dataSourcePropertyValues);
    }

    /**
     * 初始化更多数据源
     */
    private void initCustomDataSources(Environment env) {
        // 读取配置文件获取更多数据源，也可以通过defaultDataSource读取数据库获取更多数据源
        RelaxedPropertyResolver propertyResolver = new RelaxedPropertyResolver(env, "hlj.datasource.");
        String dsPrefixs = propertyResolver.getProperty("names");
        if (StringUtils.isEmpty(dsPrefixs)) return;
        for (String dsPrefix : dsPrefixs.split(",")) {// 多个数据源
            Map<String, Object> dsMap = propertyResolver.getSubProperties(dsPrefix + ".");
            DataSource ds = buildDataSource(dsMap);
            customDataSources.put(dsPrefix, ds);
            dataBinder(ds, env,"hlj.datasource." + dsPrefix);
        }
    }

    /**
     * 创建DataSource
     * @return
     */
    public DataSource buildDataSource(Map<String, Object> dsMap) {
        try {
            Object type = dsMap.get("type");
            if (type == null)
                type = DATASOURCE_TYPE_DEFAULT;// 默认DataSource

            Class<? extends DataSource> dataSourceType;
            dataSourceType = (Class<? extends DataSource>) Class.forName((String) type);

            String driverClassName = dsMap.get("driver-class-name").toString();
            String url = dsMap.get("url").toString();
            String username = dsMap.get("username").toString();
            String password = dsMap.get("password").toString();

            DataSourceBuilder factory = DataSourceBuilder.create()
                    .driverClassName(driverClassName)
                    .url(url).username(username)
                    .password(password)
                    .type(dataSourceType);
            return factory.build();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
