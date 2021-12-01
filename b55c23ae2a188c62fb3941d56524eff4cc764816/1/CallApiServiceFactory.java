package com.java110.core.factory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.context.IPageData;
import com.java110.core.context.PageData;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.exception.SMOException;
import com.java110.utils.factory.ApplicationContextFactory;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

public class CallApiServiceFactory {

    private static final String URL_API = ServiceConstant.SERVICE_API_URL + "/api/";
    //日志
    private static Logger logger = LoggerFactory.getLogger(CallApiServiceFactory.class);

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询
     *
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    public static <T> T getForApi(String appId, T param, String serviceCode, Class<T> t) {

        IPageData pd = PageData.newInstance().builder("-1", "未知", "", "", "", "", "", "", appId);

        List<T> list = getForApis(pd, param, serviceCode, t);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询
     *
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    public static <T> T postForApi(String appId, T param, String serviceCode, Class<T> t) {

        IPageData pd = PageData.newInstance().builder("-1", "未知", "", "", "", "", "", "", appId);

        List<T> ts = postForApis(pd, param, serviceCode, t);

        if (ts == null || ts.size() < 1) {
            return null;
        }

        return ts.get(0);
    }


    /**
     * 查询
     *
     * @param pd          页面对象
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    public static <T> T getForApi(IPageData pd, T param, String serviceCode, Class<T> t) {

        List<T> list = getForApis(pd, param, serviceCode, t);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 查询
     *
     * @param pd          页面对象
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    protected <T> T postForApi(IPageData pd, T param, String serviceCode, Class<T> t) {
        List<T> ts = postForApis(pd, param, serviceCode, t);

        if (ts == null || ts.size() < 1) {
            return null;
        }

        return ts.get(0);
    }

    /**
     * 查询
     *
     * @param pd          页面对象
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    protected static <T> List<T> postForApis(IPageData pd, T param, String serviceCode, Class<T> t) {

        String url = URL_API + serviceCode;
        RestTemplate restTemplate = ApplicationContextFactory.getBean("restTemplate", RestTemplate.class);


        ResponseEntity<String> responseEntity = callCenterService(restTemplate, pd, JSONObject.toJSONString(param), url, HttpMethod.POST);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new SMOException("调用" + serviceCode + "失败，" + responseEntity.getBody());
        }

        JSONObject resultVo = JSONObject.parseObject(responseEntity.getBody());

        if (ResultVo.CODE_MACHINE_OK != resultVo.getInteger("code")) {
            throw new SMOException(resultVo.getString("msg"));
        }

        Object bObj = resultVo.get("data");
        JSONArray datas = null;
        if (bObj instanceof JSONObject) {
            datas = new JSONArray();
            datas.add(bObj);
        } else {
            datas = (JSONArray) bObj;
        }
        String jsonStr = JSONObject.toJSONString(datas);

        List<T> list = JSONObject.parseArray(jsonStr, t);
        return list;
    }

    /**
     * 查询
     *
     * @param pd          页面对象
     * @param param       传入对象
     * @param serviceCode 服务编码
     * @param t           返回类
     * @param <T>
     * @return
     */
    public static <T> List<T> getForApis(IPageData pd, T param, String serviceCode, Class<T> t) {

        String url = URL_API + serviceCode;
        if (param != null) {
            url += mapToUrlParam(BeanConvertUtil.beanCovertMap(param));
        }
        RestTemplate restTemplate = ApplicationContextFactory.getBean("restTemplate", RestTemplate.class);

        ResponseEntity<String> responseEntity = callCenterService(restTemplate, pd, "", url, HttpMethod.GET);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new SMOException("调用" + serviceCode + "失败，" + responseEntity.getBody());
        }

        JSONObject resultVo = JSONObject.parseObject(responseEntity.getBody());

        if (!"0".equals(resultVo.getString("code"))) {
            throw new SMOException(resultVo.getString("msg"));
        }

        Object bObj = resultVo.get("data");
        JSONArray datas = null;
        if (bObj instanceof JSONObject) {
            datas = new JSONArray();
            datas.add(bObj);
        } else {
            datas = (JSONArray) bObj;
        }
        String jsonStr = JSONObject.toJSONString(datas);

        List<T> list = JSONObject.parseArray(jsonStr, t);
        return list;
    }

    private static ResponseEntity<String> callCenterService(RestTemplate restTemplate, IPageData pd, String param, String url, HttpMethod httpMethod) {
        ResponseEntity<String> responseEntity = null;
        HttpHeaders header = new HttpHeaders();
        header.add(CommonConstant.HTTP_APP_ID.toLowerCase(), pd.getAppId());
        header.add(CommonConstant.HTTP_USER_ID.toLowerCase(), StringUtil.isEmpty(pd.getUserId()) ? CommonConstant.ORDER_DEFAULT_USER_ID : pd.getUserId());
        header.add(CommonConstant.HTTP_TRANSACTION_ID.toLowerCase(), pd.getTransactionId());
        header.add(CommonConstant.HTTP_REQ_TIME.toLowerCase(), pd.getRequestTime());
        header.add(CommonConstant.HTTP_SIGN.toLowerCase(), "");
        HttpEntity<String> httpEntity = new HttpEntity<String>(param, header);
        //logger.debug("请求中心服务信息，{}", httpEntity);
        try {
            responseEntity = restTemplate.exchange(url, httpMethod, httpEntity, String.class);
        } catch (HttpStatusCodeException e) { //这里spring 框架 在4XX 或 5XX 时抛出 HttpServerErrorException 异常，需要重新封装一下
            responseEntity = new ResponseEntity<String>(e.getResponseBodyAsString(), e.getStatusCode());
        } catch (Exception e) {
            responseEntity = new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            logger.debug("请求地址为,{} 请求中心服务信息，{},中心服务返回信息，{}", url, httpEntity, responseEntity);
            return responseEntity;
        }

    }

    /**
     * map 参数转 url get 参数 非空值转为get参数 空值忽略
     *
     * @param info map数据
     * @return url get 参数 带？
     */
    public static String mapToUrlParam(Map info) {
        String urlParam = "";
        if (info == null || info.isEmpty()) {
            return urlParam;
        }

        urlParam += "?";

        for (Object key : info.keySet()) {
            if (StringUtil.isNullOrNone(info.get(key))) {
                continue;
            }

            urlParam += (key + "=" + info.get(key) + "&");
        }

        urlParam = urlParam.endsWith("&") ? urlParam.substring(0, urlParam.length() - 1) : urlParam;

        return urlParam;
    }
}
