package com.ctrip.apollo.biz.datasource;

import java.lang.reflect.Method;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import com.dianping.cat.Cat;

@Component
@Conditional(TitanCondition.class)
public class TitanEntityManager {

  @Autowired
  private TitanSettings settings;

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Bean
  public DataSource datasource() throws Exception {
    Class clazz = Class.forName("com.ctrip.datasource.configure.DalDataSourceFactory");
    Object obj = clazz.newInstance();
    Method method = clazz.getMethod("createDataSource", new Class[] {String.class, String.class});
    DataSource ds = ((DataSource) method.invoke(obj,
        new Object[] {settings.getTitanDbname(), settings.getTitanUrl()}));
    Cat.logEvent("Apollo.Datasource.Titan", settings.getTitanDbname());
    return ds;
  }

}
