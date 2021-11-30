package com.hlj.many.datasourse.config;

import org.apache.commons.lang3.ArrayUtils;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.IOException;



/**
 * @Description
 * @Author HealerJean
 * @Date 2018/4/24  下午5:44.
 */
@Configuration
public class MybatisConfig {

    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer(){
        MapperScannerConfigurer configurer = new MapperScannerConfigurer();
        configurer.setBasePackage("com.hlj.many.datasourse.dataresource.dao.*");
        return configurer;
    }

    @Bean
    public SqlSessionFactoryBean sessionFactory(DataSource dataSource, ApplicationContext applicationContext) throws IOException {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis.xml"));

        Resource[] resources = ArrayUtils.addAll(
                applicationContext.getResources("classpath*:com/hlj/many/datasourse/dataresource/dao/**/*.xml")
        );
        sessionFactoryBean.setMapperLocations(resources);
        return sessionFactoryBean;
    }


}

