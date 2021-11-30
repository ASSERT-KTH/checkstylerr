package com.hlj.many.datasourse.dataresource;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
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
        entityManagerFactoryRef="admoreEMF",
        transactionManagerRef="admoreTM",
        basePackages= { "com.hlj.many.datasourse.dataresource.dao.data" })
@MapperScan(basePackages = {"com.hlj.many.datasourse.dataresource.dao.data"}, sqlSessionTemplateRef  = "admoreSessionTemplate")
public class DatasourceOneData {

    @Value("${admore.datasource.url}")
    private String admoreUrl;
    @Value("${admore.datasource.username}")s
    private String admoreUsername;
    @Value("${admore.datasource.password}")
    private String admorePassword;


    @Primary
    @Bean(name = "admore")
    public DataSource primaryDataSource() {
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
//主要是为了配置实体扫描  和@EntityScan一样
    @Primary
    @Bean(name = "admoreEMF")
    public LocalContainerEntityManagerFactoryBean admoreEMF(EntityManagerFactoryBuilder builder, @Qualifier("admore") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.hlj.many.datasourse.dataresource.dao.data.entry")
                .persistenceUnit("admore")
                .build();
    }
    @Primary
    @Bean(name = "admoreTM")
    public PlatformTransactionManager admoreTransactionManager(@Qualifier("admoreEMF") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean ) {
        return new JpaTransactionManager(entityManagerFactoryBean.getObject());
    }





    @Primary
    @Bean(name = "admoreSessionFactory")
    public SqlSessionFactory admoreSessionFactory(@Qualifier("admore")DataSource dataSource, ApplicationContext applicationContext) throws Exception {
        SqlSessionFactoryBean sessionFactoryBean = new SqlSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(applicationContext.getResource("classpath:mybatis.xml"));
       Resource[] resources = ArrayUtils.addAll(
                applicationContext.getResources("classpath:/com/hlj/many/datasourse/dataresource/dao/data/mysql/*.xml")
//             ,applicationContext.getResources("classpath*:com/duodian/admore/dao/db/**/mysql/*.xml")
      );

        sessionFactoryBean.setMapperLocations(resources);
        return sessionFactoryBean.getObject();
    }

    @Primary
    @Bean(name = "admoreSessionTemplate")
    public SqlSessionTemplate testSqlSessionTemplate(@Qualifier("admoreSessionFactory") SqlSessionFactory sqlSessionFactory) throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }


}
