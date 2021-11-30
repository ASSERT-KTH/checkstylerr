package com.hlj.proj.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;


/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName DatasourceOneData
 * @date 2019/6/13  19:09.
 * @Description
 */
@Configuration
//jpa 扫描配置
//第一种
@EnableJpaRepositories(basePackages = {"com.hlj.proj.data.dao.db"})
@EntityScan(basePackages = {"com.hlj.proj.data.pojo"})

@MapperScan(basePackages = {"com.hlj.proj.data.dao.mybatis"})
@PropertySource("classpath:db.properties")
public class DatasourceConfig {

    @Value("${hlj.datasource.url}")
    private String admoreUrl;
    @Value("${hlj.datasource.username}")
    private String admoreUsername;
    @Value("${hlj.datasource.password}")
    private String admorePassword;


    @Bean(name = "dataSource")
    public DataSource dataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(admoreUrl);
        druidDataSource.setUsername(admoreUsername);
        druidDataSource.setPassword(admorePassword);
        druidDataSource.setMaxActive(150);
        druidDataSource.setInitialSize(10);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setMaxWait(3000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);

        return druidDataSource;
    }


    /**
     * 配置mybatis
     *
     * @param dataSource
     * @param applicationContext
     * @return
     * @throws Exception
     */
    @Bean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dataSource") DataSource dataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis.xml"));
        Resource[] resources = ArrayUtils.addAll(
                applicationContext.getResources("classpath*:com/hlj/proj/data/dao/mybatis/**/mysql/*.xml")
        );
        sessionFactoryBean.setMapperLocations(resources);
        return sessionFactoryBean.getObject();
    }

}

