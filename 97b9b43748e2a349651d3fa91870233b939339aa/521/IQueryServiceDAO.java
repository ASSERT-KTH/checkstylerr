package com.java110.service.dao;

import com.java110.entity.order.ServiceBusiness;
import com.java110.entity.service.ServiceSql;

import javax.validation.OverridesAttribute;
import java.util.List;
import java.util.Map;

/**
 * 查询数据交互接口
 * Created by wuxw on 2018/4/20.
 */
public interface IQueryServiceDAO {

    /**
     * 执行sql
     * @param sql
     * @return
     */
    public List<Map<String,Object>> executeSql(String sql,Object []params);

    public int updateSql(String sql,Object[] params);

    /**
     * 执行存储过程
     * @param paramsInfo
     * @return
     */
    public String executeProc(Map<String,Object> paramsInfo);


    public String updateProc(Map<String,Object> paramsInfo);

    public List<ServiceSql> qureyServiceSqlAll();

    /**
     * 查询服务业务信息
     * @return
     */
    public List<ServiceBusiness> qureyServiceBusiness();
}
