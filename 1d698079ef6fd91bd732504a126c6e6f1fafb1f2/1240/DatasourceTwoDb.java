package com.hlj.many.datasourse.dataresource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/10
 * version：1.0.0
 */

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef="admoreDataEMF",
        transactionManagerRef="admoreDataTM",
        basePackages= { "com.hlj.many.datasourse.dataresource.dao.data" })
        @MapperScan(basePackages = {"com.hlj.many.datasourse.dataresource.dao.data"}, sqlSessionTemplateRef  = "admoreDataSessionTemplate")
//@MapperScan(basePackages = {"com.hlj.many.datasourse.dataresource.dao.db","com.hlj.many.datasourse.dataresource.dao.data.mydata"}, sqlSessionTemplateRef  = "admoreSessionTemplate")
public class DatasourceTwoDb {

    @Value("${admore.data.datasource.url}")
    private String admoreDataUrl;
    @Value("${admore.data.datasource.username}")
    private String admoreDataUsername;
    @Value("${admore.data.datasource.password}")
    private String admoreDataPassword;

    @Bean(name = "admoreData")
    public DataSource primaryDataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(admoreDataUrl);
        druidDataSource.setUsername(admoreDataUsername);
        druidDataSource.setPassword(admoreDataPassword);
        druidDataSource.setMaxActive(150);
        druidDataSource.setInitialSize(10);
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setMaxWait(3000);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(300000);

        return druidDataSource;
    }

    @Bean(name = "admoreDataEMF")
    public LocalContainerEntityManagerFactoryBean admoreDataEMF(EntityManagerFactoryBuilder builder, @Qualifier("admoreData") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.hlj.many.datasourse.dataresource.dao.data.entry")
                .persistenceUnit("admoreData")
                .build();
    }

    @Bean(name = "admoreDataTM")
    public PlatformTransactionManager admoreTransactionManager(@Qualifier("admoreDataEMF") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean ) {
        return new JpaTransactionManager(entityManagerFactoryBean.getObject());
    }

    @Bean(name = "admoreDataSessionFactory")
    public SqlSessionFactory admoreDataSessionFactory(@Qualifier("admoreData") DataSource dataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis.xml"));
        Resource[] resources = ArrayUtils.addAll(
                applicationContext.getResources("classpath:/com/hlj/many/datasourse/dataresource/dao/data/mysql/*.xml")
//                applicationContext.getResources("classpath*:com/duodian/admore/dao/mydata/**/mysql/*.xml")
        );
        sessionFactoryBean.setMapperLocations(resources);
        return sessionFactoryBean.getObject();
    }



    @Bean(name = "admoreDataSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("admoreDataSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
