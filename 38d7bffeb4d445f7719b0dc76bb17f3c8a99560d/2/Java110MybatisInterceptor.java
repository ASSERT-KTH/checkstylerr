package com.java110.db;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.client.RestTemplate;
import com.java110.core.factory.Java110TransactionalFactory;
import com.java110.db.dao.IQueryServiceDAO;
import com.java110.dto.order.OrderItemDto;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.DateUtil;
import com.java110.utils.util.StringUtil;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
                Object.class})
})
public class Java110MybatisInterceptor implements Interceptor {
    private static Logger logger = LoggerFactory.getLogger(Java110MybatisInterceptor.class);

    IQueryServiceDAO queryServiceDAOImpl;
    RestTemplate restTemplate;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (StringUtil.isEmpty(Java110TransactionalFactory.getOId())) { //未开启事务
            return invocation.proceed();
        }
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        Object parameter = null;
        if (invocation.getArgs().length > 1) {
            parameter = invocation.getArgs()[1];
        }

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);
        Configuration configuration = mappedStatement.getConfiguration();
        //获取sql语句
        String sql = showSql(configuration, boundSql);
        restTemplate = ApplicationContextFactory.getBean("restTemplate", RestTemplate.class);
        switch (sqlCommandType) {
            case INSERT:
                dealInsertSql(mappedStatement, parameter, sql);
                break;
            case UPDATE:
                dealUpdateSql(mappedStatement, parameter, sql);
                break;
            case DELETE:
                dealDeleteSql(mappedStatement, parameter, sql);
                break;
        }
        return invocation.proceed();
    }

    /**
     * 处理删除sql
     *
     * @param mappedStatement
     * @param parameter
     */
    private void dealDeleteSql(MappedStatement mappedStatement, Object parameter, String sql) {

        String tmpTable = sql.substring(sql.indexOf("into") + 4, sql.indexOf("("));
        String tmpWhere = sql.substring(sql.indexOf("where"));
        //插入操作时之前的 没有数据 所以 preValue 为空对象
        JSONArray preValues = new JSONArray();

        String execSql = "select * from " + tmpTable + " " + tmpWhere;

        queryServiceDAOImpl = ApplicationContextFactory.getBean("queryServiceDAOImpl", IQueryServiceDAO.class);
        List<Map<String, Object>> deleteDatas = queryServiceDAOImpl.executeSql(execSql, null);

        if (deleteDatas != null && deleteDatas.size() > 0) {
            for (Map<String, Object> map : deleteDatas) {
                dealReturnMap(map);
                preValues.add(map);
            }
        }

        JSONArray afterValues = new JSONArray();

        JSONObject logText = new JSONObject();
        logText.put("preValue", preValues);
        logText.put("afterValue", afterValues);

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setbId("-1");
        orderItemDto.setAction("DEL");
        orderItemDto.setActionObj(tmpTable.trim());
        orderItemDto.setLogText(logText.toJSONString());
        orderItemDto.setServiceName(ApplicationContextFactory.getApplicationName());
        orderItemDto.setoId(Java110TransactionalFactory.getOId());

        String url = ServiceConstant.SERVICE_ORDER_URL + "/order/oIdApi/createOrderItem";
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(orderItemDto.toString(), httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("注册事务回滚日志失败" + responseEntity);
        }
    }

    /**
     * 处理修改 sql
     *
     * @param mappedStatement
     * @param parameter
     */
    private void dealUpdateSql(MappedStatement mappedStatement, Object parameter, String sql) {
        //RestTemplate restTemplate = ApplicationContextFactory.getBean("restTemplate", RestTemplate.class);

        String tmpTable = sql.substring(sql.indexOf("update") + 6, sql.indexOf("set"));
        String tmpWhere = sql.substring(sql.indexOf("where"));
        String tmpKey = sql.substring(sql.indexOf("set") + 3, sql.indexOf("where"));
        String[] tmpString = tmpKey.split(",");
        Map tmpAfterMap = new HashMap();
        for (String tmp : tmpString) {
            String[] keyValues = tmp.split("=");
            String key = "";
            if (keyValues.length != 2) {
                throw new IllegalArgumentException("update 语句可能有问题，没有 set 中出错 " + sql);
            }
            if (keyValues[0].contains(".")) {
                key = keyValues[0].substring(keyValues[0].indexOf(".") + 1);
            } else {
                key = keyValues[0];
            }
            tmpAfterMap.put(key.trim(), keyValues[1].trim());
        }


        if (tmpString == null || tmpString.length < 1) {
            throw new IllegalArgumentException("update 语句可能有问题，没有 set 内容 " + sql);
        }
        //插入操作时之前的 没有数据 所以 preValue 为空对象
        JSONArray preValues = new JSONArray();
        JSONArray afterValues = new JSONArray();
        JSONObject afterVaule = null;
        String execSql = "select * from " + tmpTable + " " + tmpWhere;

        queryServiceDAOImpl = ApplicationContextFactory.getBean("queryServiceDAOImpl", IQueryServiceDAO.class);
        List<Map<String, Object>> deleteDatas = queryServiceDAOImpl.executeSql(execSql, null);

        if (deleteDatas != null && deleteDatas.size() > 0) {
            for (Map<String, Object> map : deleteDatas) {
                dealReturnMap(map);
                preValues.add(map);
                afterVaule = new JSONObject();
                afterVaule.putAll(map);
                afterVaule.putAll(tmpAfterMap);
                afterValues.add(afterVaule);
            }
        }

        JSONObject logText = new JSONObject();
        logText.put("preValue", preValues);
        logText.put("afterValue", afterValues);

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setbId("-1");
        orderItemDto.setAction("MOD");
        orderItemDto.setActionObj(tmpTable.trim());
        orderItemDto.setLogText(logText.toJSONString());
        orderItemDto.setServiceName(ApplicationContextFactory.getApplicationName());
        orderItemDto.setoId(Java110TransactionalFactory.getOId());

        String url = ServiceConstant.SERVICE_ORDER_URL + "/order/oIdApi/createOrderItem";
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(orderItemDto.toString(), httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("注册事务回滚日志失败" + responseEntity);
        }
    }

    private void dealReturnMap(Map<String, Object> map) {
        for (String key : map.keySet()) {
            Object value = map.get(key);
            if (value instanceof String) {
                map.put(key, "'" + map.get(key) + "'");
            } else if (value instanceof Date) {
                String tmpValue = DateUtil.getFormatTimeString((Date) value, DateUtil.DATE_FORMATE_STRING_A);
                map.put(key, "'" + tmpValue + "'");
            } else if (value instanceof Timestamp) {
                Date date = new Date(((Timestamp) value).getTime());
                String tmpValue = DateUtil.getFormatTimeString(date, DateUtil.DATE_FORMATE_STRING_A);
                map.put(key, "'" + tmpValue + "'");
            } else if(value instanceof Double){
                map.put(key, "'" + map.get(key) + "'");
            }else {
                if (value != null) {
                    map.put(key, "'" + value.toString() + "'");
                } else {
                    map.put(key, "''");
                }
            }
        }
    }

    /**
     * 处理insert 语句
     *
     * @param mappedStatement
     * @param parameter
     */
    private void dealInsertSql(MappedStatement mappedStatement, Object parameter, String sql) {

        // RestTemplate restTemplate = ApplicationContextFactory.getBean("restTemplate", RestTemplate.class);

        //插入操作时之前的 没有数据 所以 preValue 为空对象
        JSONArray preValues = new JSONArray();

        JSONArray afterValues = new JSONArray();
        JSONObject afterValue = new JSONObject();
        String tmpTable = sql.substring(sql.toLowerCase().indexOf("into") + 4, sql.indexOf("("));
        String tmpKey = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
        String tmpValue = sql.substring(sql.lastIndexOf("(") + 1, sql.lastIndexOf(")"));
        String[] tmpKeys = tmpKey.split(",");
        String[] tmpValues = tmpValue.split(",");

        if (tmpKeys.length != tmpValues.length) {
            throw new IllegalArgumentException("sql 错误 key 和value 个数不等" + sql);
        }

        if (tmpKeys.length < 1) {
            throw new IllegalArgumentException("sql 错误 未找到key" + sql);
        }
        for (int keyIndex = 0; keyIndex < tmpKeys.length; keyIndex++) {
            afterValue.put(tmpKeys[keyIndex], tmpValues[keyIndex]);
        }
        afterValues.add(afterValue);

        JSONObject logText = new JSONObject();
        logText.put("preValue", preValues);
        logText.put("afterValue", afterValues);

        OrderItemDto orderItemDto = new OrderItemDto();
        orderItemDto.setbId("-1");
        orderItemDto.setAction("ADD");
        orderItemDto.setActionObj(tmpTable.trim());
        orderItemDto.setLogText(logText.toJSONString());
        orderItemDto.setServiceName(ApplicationContextFactory.getApplicationName());
        orderItemDto.setoId(Java110TransactionalFactory.getOId());

        String url = ServiceConstant.SERVICE_ORDER_URL + "/order/oIdApi/createOrderItem";
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity httpEntity = new HttpEntity(orderItemDto.toString(), httpHeaders);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("注册事务回滚日志失败" + responseEntity);
        }

    }

    @Override
    public Object plugin(Object target) {

        return Plugin.wrap(target, this);

    }

    @Override
    public void setProperties(Properties properties) {

    }

    public String showSql(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql().replaceAll("[\\s]+", " ");
        if (parameterMappings.size() > 0 && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = sql.replaceFirst("\\?", getParameterValue(parameterObject));

            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = sql.replaceFirst("\\?", getParameterValue(obj));
                    }
                }
            }
        }
        return sql.toLowerCase();
    }

    private String getParameterValue(Object obj) {
        String value = null;
        if (obj instanceof String) {
            value = "'" + obj.toString() + "'";
        } else if (obj instanceof Date) {
            DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            value = "'" + formatter.format(obj) + "'";
//	            System.out.println(value);
        } else {
            if (obj != null) {
                value = obj.toString();
            } else {
                value = "";
            }

        }
        return value;
    }
}
