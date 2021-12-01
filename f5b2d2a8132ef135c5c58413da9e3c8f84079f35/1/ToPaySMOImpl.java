package com.java110.front.smo.payment.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.core.context.IPageData;
import com.java110.core.context.PageData;
import com.java110.dto.app.AppDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.front.properties.WechatAuthProperties;
import com.java110.front.smo.AppAbstractComponentSMO;
import com.java110.front.smo.payment.IToPaySMO;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service("toPaySMOImpl")
public class ToPaySMOImpl extends AppAbstractComponentSMO implements IToPaySMO {
    private static final Logger logger = LoggerFactory.getLogger(AppAbstractComponentSMO.class);


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RestTemplate outRestTemplate;


    @Autowired
    private WechatAuthProperties wechatAuthProperties;

    @Override
    public ResponseEntity<String> toPay(IPageData pd) {
        return super.businessProcess(pd);
    }

    @Override
    protected void validate(IPageData pd, JSONObject paramIn) {

        Assert.jsonObjectHaveKey(paramIn, "communityId", "请求报文中未包含communityId节点");
        Assert.jsonObjectHaveKey(paramIn, "cycles", "请求报文中未包含cycles节点");
        Assert.jsonObjectHaveKey(paramIn, "receivedAmount", "请求报文中未包含receivedAmount节点");
        Assert.jsonObjectHaveKey(paramIn, "feeId", "请求报文中未包含feeId节点");
        Assert.jsonObjectHaveKey(paramIn, "feeName", "请求报文中未包含feeName节点");
        Assert.jsonObjectHaveKey(paramIn, "appId", "请求报文中未包含appId节点");

    }

    @Override
    protected ResponseEntity<String> doBusinessProcess(IPageData pd, JSONObject paramIn) throws Exception {

        ResponseEntity responseEntity = null;

        SmallWeChatDto smallWeChatDto = getSmallWechat(pd, paramIn);

        if (smallWeChatDto == null) { //从配置文件中获取 小程序配置信息
            smallWeChatDto = new SmallWeChatDto();
            smallWeChatDto.setAppId(wechatAuthProperties.getAppId());
            smallWeChatDto.setAppSecret(wechatAuthProperties.getSecret());
            smallWeChatDto.setMchId(wechatAuthProperties.getMchId());
            smallWeChatDto.setPayPassword(wechatAuthProperties.getKey());
        }

        //查询用户ID
        paramIn.put("userId", pd.getUserId());
        String url = ServiceConstant.SERVICE_API_URL + "/api/fee.payFeePre";
        responseEntity = super.callCenterService(restTemplate, pd, paramIn.toJSONString(), url, HttpMethod.POST);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }
        JSONObject orderInfo = JSONObject.parseObject(responseEntity.getBody().toString());
        String orderId = orderInfo.getString("oId");
        double money = Double.parseDouble(orderInfo.getString("receivableAmount"));
        String appType = OwnerAppUserDto.APP_TYPE_WECHAT_MINA;
        if (AppDto.WECHAT_OWNER_APP_ID.equals(pd.getAppId())) {
            appType = OwnerAppUserDto.APP_TYPE_WECHAT;
        }else if(AppDto.WECHAT_MINA_OWNER_APP_ID.equals(pd.getAppId())){
            appType = OwnerAppUserDto.APP_TYPE_WECHAT_MINA;
        }else{
            appType = OwnerAppUserDto.APP_TYPE_APP;
        }
        Map tmpParamIn = new HashMap();
        tmpParamIn.put("userId", pd.getUserId());
        tmpParamIn.put("appType", appType);
        responseEntity = super.getOwnerAppUser(pd, restTemplate, tmpParamIn);
        logger.debug("查询用户信息返回报文：" + responseEntity);
        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new IllegalArgumentException("未查询用户信息异常" + tmpParamIn);
        }

        JSONObject userResult = JSONObject.parseObject(responseEntity.getBody().toString());
        int total = userResult.getIntValue("total");
        if (total < 1) {
            //未查询到用户信息
            throw new IllegalArgumentException("未查询微信用户");
        }

        JSONObject realUserInfo = userResult.getJSONArray("data").getJSONObject(0);

        String openId = realUserInfo.getString("openId");
//        String payAppId = orderInfo.getString("payAppId");
//        String payMchId = orderInfo.getString("payMchId");


        Map result = super.java110Payment(outRestTemplate, paramIn.getString("feeName"), paramIn.getString("tradeType"), orderId, money, openId, smallWeChatDto);
        responseEntity = new ResponseEntity(JSONObject.toJSONString(result), HttpStatus.OK);

        return responseEntity;
    }


    private SmallWeChatDto getSmallWechat(IPageData pd, JSONObject paramIn) {

        ResponseEntity responseEntity = null;

        pd = PageData.newInstance().builder(pd.getUserId(), "", "", pd.getReqData(),
                "", "", "", "",
                pd.getAppId());
        responseEntity = this.callCenterService(restTemplate, pd, "",
                ServiceConstant.SERVICE_API_URL + "/api/smallWeChat.listSmallWeChats?appId="
                        + paramIn.getString("appId") + "&page=1&row=1", HttpMethod.GET);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            return null;
        }
        JSONObject smallWechatObj = JSONObject.parseObject(responseEntity.getBody().toString());
        JSONArray smallWeChats = smallWechatObj.getJSONArray("smallWeChats");

        if (smallWeChats == null || smallWeChats.size() < 1) {
            return null;
        }

        return BeanConvertUtil.covertBean(smallWeChats.get(0), SmallWeChatDto.class);
    }


}
