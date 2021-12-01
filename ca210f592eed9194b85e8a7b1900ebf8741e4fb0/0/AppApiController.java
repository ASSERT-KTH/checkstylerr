package com.java110.app.controller;

import com.alibaba.fastjson.JSONObject;
import com.java110.app.smo.api.IApiSMO;
import com.java110.app.smo.wxLogin.IWxLoginSMO;
import com.java110.core.base.controller.BaseController;
import com.java110.core.context.IPageData;
import com.java110.core.context.PageData;
import com.java110.dto.wxLogin.UserInfo;
import com.java110.dto.wxLogin.WxLoginInfo;
import com.java110.utils.constant.CommonConstant;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序api处理类
 *
 * 主要用于透传api 直接提供出来的接口
 *
 * 方便快速开发
 *
 * add by wuxw 2019-11-19
 */
@RestController
@RequestMapping(path = "/api")
public class AppApiController extends BaseController {
    private final static Logger logger = LoggerFactory.getLogger(AppApiController.class);

    @Autowired
    private IApiSMO apiSMOImpl;

    /**
     * 资源请求 post方式
     *
     * @param service  请求接口方式
     * @param postInfo post内容
     * @param request  请求对象 查询头信息 url等信息
     * @return http status 200 成功 其他失败
     */

    @RequestMapping(path = "/{service:.+}", method = RequestMethod.POST)
    @ApiOperation(value = "资源post请求", notes = "test: 返回 2XX 表示服务正常")
    @ApiImplicitParam(paramType = "query", name = "service", value = "用户编号", required = true, dataType = "String")
    public ResponseEntity<String> servicePost(@PathVariable String service,
                                              @RequestBody String postInfo,
                                              HttpServletRequest request) {
        ResponseEntity<String> responseEntity = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            this.getRequestInfo(request, headers);
            headers.put(CommonConstant.HTTP_SERVICE, service);
            headers.put(CommonConstant.HTTP_METHOD, CommonConstant.HTTP_METHOD_POST);
            logger.debug("api：{} 请求报文为：{},header信息为：{}", service, postInfo, headers);
            responseEntity = apiSMOImpl.doApi(postInfo, headers);
        } catch (Throwable e) {
            logger.error("请求post 方法[" + service + "]失败：" + postInfo, e);
            responseEntity = new ResponseEntity<String>("请求发生异常，" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.debug("api：{} 返回信息为：{}", service, responseEntity);

        return responseEntity;
    }

    /**
     * 资源请求 get方式
     *
     * @param service 请求接口方式
     * @param request 请求对象 查询头信息 url等信息
     * @return http status 200 成功 其他失败
     */

    @RequestMapping(path = "/{service:.+}", method = RequestMethod.GET)
    @ApiOperation(value = "资源get请求", notes = "test: 返回 2XX 表示服务正常")
    @ApiImplicitParam(paramType = "query", name = "service", value = "用户编号", required = true, dataType = "String")
    public ResponseEntity<String> serviceGet(@PathVariable String service,
                                             HttpServletRequest request) {
        ResponseEntity<String> responseEntity = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            this.getRequestInfo(request, headers);
            headers.put(CommonConstant.HTTP_SERVICE, service);
            headers.put(CommonConstant.HTTP_METHOD, CommonConstant.HTTP_METHOD_GET);
            logger.debug("api：{} 请求报文为：{},header信息为：{}", "", headers);
            responseEntity = apiSMOImpl.doApi(JSONObject.toJSONString(getParameterStringMap(request)), headers);
        } catch (Throwable e) {
            logger.error("请求get 方法[" + service + "]失败：", e);
            responseEntity = new ResponseEntity<String>("请求发生异常，" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.debug("api：{} 返回信息为：{}", service, responseEntity);

        return responseEntity;
    }

    /**
     * 资源请求 put方式
     *
     * @param service  请求接口方式
     * @param postInfo 修改内容
     * @param request  请求对象 查询头信息 url等信息
     * @return http status 200 成功 其他失败
     */

    @RequestMapping(path = "/{service:.+}", method = RequestMethod.PUT)
    @ApiOperation(value = "资源put请求", notes = "test: 返回 2XX 表示服务正常")
    @ApiImplicitParam(paramType = "query", name = "service", value = "用户编号", required = true, dataType = "String")
    public ResponseEntity<String> servicePut(@PathVariable String service,
                                             @RequestBody String postInfo,
                                             HttpServletRequest request) {
        ResponseEntity<String> responseEntity = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            this.getRequestInfo(request, headers);
            headers.put(CommonConstant.HTTP_SERVICE, service);
            headers.put(CommonConstant.HTTP_METHOD, CommonConstant.HTTP_METHOD_PUT);
            logger.debug("api：{} 请求报文为：{},header信息为：{}", service, postInfo, headers);
            responseEntity = apiSMOImpl.doApi(postInfo, headers);
        } catch (Throwable e) {
            logger.error("请求put 方法[" + service + "]失败：", e);
            responseEntity = new ResponseEntity<String>("请求发生异常，" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        logger.debug("api：{} 返回信息为：{}", service, responseEntity);
        return responseEntity;
    }

    /**
     * 资源请求 delete方式
     *
     * @param service 请求接口方式
     * @param request 请求对象 查询头信息 url等信息
     * @return http status 200 成功 其他失败
     */

    @RequestMapping(path = "/{service:.+}", method = RequestMethod.DELETE)
    @ApiOperation(value = "资源delete请求", notes = "test: 返回 2XX 表示服务正常")
    @ApiImplicitParam(paramType = "query", name = "service", value = "用户编号", required = true, dataType = "String")
    public ResponseEntity<String> serviceDelete(@PathVariable String service,
                                                HttpServletRequest request) {
        ResponseEntity<String> responseEntity = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            this.getRequestInfo(request, headers);
            headers.put(CommonConstant.HTTP_SERVICE, service);
            headers.put(CommonConstant.HTTP_METHOD, CommonConstant.HTTP_METHOD_DELETE);
            logger.debug("api：{} 请求报文为：{},header信息为：{}", service, "", headers);

            responseEntity = apiSMOImpl.doApi(JSONObject.toJSONString(getParameterStringMap(request)), headers);
        } catch (Throwable e) {
            logger.error("请求delete 方法[" + service + "]失败：", e);
            responseEntity = new ResponseEntity<String>("请求发生异常，" + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        logger.debug("api：{} 返回信息为：{}", service, responseEntity);
        return responseEntity;
    }


    /**
     * 获取请求信息
     *
     * @param request
     * @param headers
     * @throws RuntimeException
     */
    private void getRequestInfo(HttpServletRequest request, Map headers) throws Exception {
        try {
            super.initHeadParam(request, headers);
            super.initUrlParam(request, headers);
        } catch (Exception e) {
            logger.error("加载头信息失败", e);
            throw e;
        }
    }

    public IApiSMO getApiSMOImpl() {
        return apiSMOImpl;
    }

    public void setApiSMOImpl(IApiSMO apiSMOImpl) {
        this.apiSMOImpl = apiSMOImpl;
    }
}
