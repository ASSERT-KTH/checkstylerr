package com.java110.job.adapt.payment.notice;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.java110.core.factory.WechatFactory;
import com.java110.core.smo.IComputeFeeSMO;
import com.java110.dto.basePrivilege.BasePrivilegeDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.fee.FeeDetailDto;
import com.java110.dto.fee.FeeDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerCarDto;
import com.java110.dto.owner.OwnerDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.staffAppAuth.StaffAppAuthDto;
import com.java110.dto.user.UserDto;
import com.java110.entity.order.Business;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.ICommunityInnerServiceSMO;
import com.java110.intf.fee.IFeeDetailInnerServiceSMO;
import com.java110.intf.fee.IFeeInnerServiceSMO;
import com.java110.intf.order.IPrivilegeInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.*;
import com.java110.job.adapt.DatabusAdaptImpl;
import com.java110.po.fee.PayFeeDetailPo;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * 缴费通知适配器
 *
 * @author fqz
 * @date 2020-12-11  18:54
 */
@Component(value = "machinePaymentNoticeAdapt")
public class MachinePaymentNoticeAdapt extends DatabusAdaptImpl {

    private static Logger logger = LoggerFactory.getLogger(MachinePaymentNoticeAdapt.class);

    @Autowired
    private ICommunityInnerServiceSMO communityInnerServiceSMO;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IPrivilegeInnerServiceSMO privilegeInnerServiceSMO;

    @Autowired
    private RestTemplate outRestTemplate;

    @Autowired
    private IStaffAppAuthInnerServiceSMO staffAppAuthInnerServiceSMO;

    @Autowired
    private IFeeInnerServiceSMO feeInnerServiceSMOImpl;

    @Autowired
    private IFeeDetailInnerServiceSMO feeDetailInnerServiceSMOImpl;

    @Autowired
    private IComputeFeeSMO computeFeeSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMO;

    @Autowired
    private IOwnerCarInnerServiceSMO ownerCarInnerServiceSMO;

    @Autowired
    private IOwnerInnerServiceSMO ownerInnerServiceSMO;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMO;

    //模板信息推送地址
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";

    public final static String ALI_SMS_DOMAIN = "ALI_SMS";

    @Override
    public void execute(Business business, List<Business> businesses) {
        JSONObject data = business.getData();
        JSONArray businessPayFeeDetails = null;
        if (data == null) {
            FeeDetailDto feeDetailDto = new FeeDetailDto();
            feeDetailDto.setbId(business.getbId());
            List<FeeDetailDto> feeDetailDtos = feeDetailInnerServiceSMOImpl.queryFeeDetails(feeDetailDto);
            Assert.listOnlyOne(feeDetailDtos, "未查询到缴费记录");
            businessPayFeeDetails = JSONArray.parseArray(JSONArray.toJSONString(feeDetailDtos));
        } else if (data.containsKey(PayFeeDetailPo.class.getSimpleName())) {
            Object bObj = data.get(PayFeeDetailPo.class.getSimpleName());
            if (bObj instanceof JSONObject) {
                businessPayFeeDetails = new JSONArray();
                businessPayFeeDetails.add(bObj);
            } else if (bObj instanceof Map) {
                businessPayFeeDetails = new JSONArray();
                businessPayFeeDetails.add(JSONObject.parseObject(JSONObject.toJSONString(bObj)));
            } else if (bObj instanceof List) {
                businessPayFeeDetails = JSONArray.parseArray(JSONObject.toJSONString(bObj));
            } else {
                businessPayFeeDetails = (JSONArray) bObj;
            }
        } else {
            return;
        }
        for (int bPayFeeDetailIndex = 0; bPayFeeDetailIndex < businessPayFeeDetails.size(); bPayFeeDetailIndex++) {
            JSONObject businessPayFeeDetail = businessPayFeeDetails.getJSONObject(bPayFeeDetailIndex);
            doSendPayFeeDetail(business, businessPayFeeDetail);
        }
    }

    private void doSendPayFeeDetail(Business business, JSONObject businessPayFeeDetail) {
        //查询缴费明细
        PayFeeDetailPo payFeeDetailPo = BeanConvertUtil.covertBean(businessPayFeeDetail, PayFeeDetailPo.class);
        //查询小区信息
        CommunityDto communityDto = new CommunityDto();
        communityDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<CommunityDto> communityDtos = communityInnerServiceSMO.queryCommunitys(communityDto);
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);

        Assert.listOnlyOne(feeDtos, "未查询到费用信息");
        //获取费用类型
        String feeTypeCdName = feeDtos.get(0).getFeeTypeCdName();
        //获取缴费用户楼栋单元房间号
        String payerObjName = computeFeeSMOImpl.getFeeObjName(feeDtos.get(0));
        //获得用户缴费开始时间
        String startTime = payFeeDetailPo.getStartTime();
        //获取用户缴费到期时间
        String endTime = payFeeDetailPo.getEndTime();
        //获取用户缴费金额
        String receivedAmount = payFeeDetailPo.getReceivedAmount();
        //获取社区名称
        String name = communityDtos.get(0).getName();
        JSONObject paramIn = new JSONObject();
        paramIn.put("payFeeRoom", name + payerObjName);
        paramIn.put("feeTypeCdName", feeTypeCdName);
        paramIn.put("payFeeTime", startTime + "至" + endTime);
        paramIn.put("receivedAmount", receivedAmount);
        paramIn.put("startTime", startTime);
        paramIn.put("endTime", endTime);
        sendMessage(paramIn, communityDtos.get(0), payFeeDetailPo);
        publishMsg(paramIn, communityDtos.get(0), payFeeDetailPo);
    }

    /**
     * 给员工推送信息
     *
     * @param paramIn
     * @param communityDto
     * @param payFeeDetailPo
     */
    private void publishMsg(JSONObject paramIn, CommunityDto communityDto, PayFeeDetailPo payFeeDetailPo) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SUCCESS_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (accessToken == null || accessToken == "") {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "费用不存在");
        // 根据特定权限查询 有该权限的 员工
        BasePrivilegeDto basePrivilegeDto = new BasePrivilegeDto();
        //basePrivilegeDto.setPId("502020121454780004");
        basePrivilegeDto.setResource("/wechatNotification");
        basePrivilegeDto.setStoreId(feeDtos.get(0).getIncomeObjId());
        List<UserDto> userDtos = privilegeInnerServiceSMO.queryPrivilegeUsers(basePrivilegeDto);
        String url = sendMsgUrl + accessToken;
        for (UserDto userDto : userDtos) {
            //根据 userId 查询到openId
            StaffAppAuthDto staffAppAuthDto = new StaffAppAuthDto();
            staffAppAuthDto.setStaffId(userDto.getUserId());
            staffAppAuthDto.setAppType("WECHAT");
            List<StaffAppAuthDto> staffAppAuthDtos = staffAppAuthInnerServiceSMO.queryStaffAppAuths(staffAppAuthDto);
            String openId = staffAppAuthDtos.get(0).getOpenId();
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("本次缴费已到账"));
            data.setKeyword1(new Content(paramIn.getString("payFeeRoom")));
            data.setKeyword2(new Content(paramIn.getString("feeTypeCdName")));
            data.setKeyword3(new Content(paramIn.getString("payFeeTime")));
            data.setKeyword4(new Content(paramIn.getString("receivedAmount") + "元"));
            data.setRemark(new Content("感谢您的使用,如有疑问请联系相关物业人员"));
            templateMessage.setData(data);
            String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("微信模板返回内容:{}", responseEntity);
        }
    }

    /**
     * 给业主推送消息
     *
     * @param paramIn
     * @param communityDto
     * @param payFeeDetailPo
     */
    private void sendMessage(JSONObject paramIn, CommunityDto communityDto, PayFeeDetailPo payFeeDetailPo) {
        //查询公众号配置
        SmallWeChatDto smallWeChatDto = new SmallWeChatDto();
        smallWeChatDto.setWeChatType("1100");
        smallWeChatDto.setObjType(SmallWeChatDto.OBJ_TYPE_COMMUNITY);
        smallWeChatDto.setObjId(communityDto.getCommunityId());
        List<SmallWeChatDto> smallWeChatDtos = smallWeChatInnerServiceSMOImpl.querySmallWeChats(smallWeChatDto);
        if (smallWeChatDto == null || smallWeChatDtos.size() <= 0) {
            logger.info("未配置微信公众号信息,定时任务执行结束");
            return;
        }
        SmallWeChatDto weChatDto = smallWeChatDtos.get(0);
        SmallWechatAttrDto smallWechatAttrDto = new SmallWechatAttrDto();
        smallWechatAttrDto.setCommunityId(communityDto.getCommunityId());
        smallWechatAttrDto.setWechatId(weChatDto.getWeChatId());
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_SUCCESS_TEMPLATE);
        List<SmallWechatAttrDto> smallWechatAttrDtos = smallWechatAttrInnerServiceSMOImpl.querySmallWechatAttrs(smallWechatAttrDto);
        if (smallWechatAttrDtos == null || smallWechatAttrDtos.size() <= 0) {
            logger.info("未配置微信公众号消息模板");
            return;
        }
        String templateId = smallWechatAttrDtos.get(0).getValue();
        String accessToken = WechatFactory.getAccessToken(weChatDto.getAppId(), weChatDto.getAppSecret());
        if (accessToken == null || accessToken == "") {
            logger.info("推送微信模板,获取accessToken失败:{}", accessToken);
            return;
        }
        FeeDto feeDto = new FeeDto();
        feeDto.setFeeId(payFeeDetailPo.getFeeId());
        feeDto.setCommunityId(payFeeDetailPo.getCommunityId());
        List<FeeDto> feeDtos = feeInnerServiceSMOImpl.queryFees(feeDto);
        Assert.listOnlyOne(feeDtos, "费用不存在");
        //支付房间id
        String payerObjId = feeDtos.get(0).getPayerObjId();
        //支付类型(房屋、车辆)
        String payerObjType = feeDtos.get(0).getPayerObjType();
        String ownerId = "";
        //3333 房屋缴费 6666 是车位缴费
        if (payerObjType.equals("3333")) {
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(payerObjId);
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMO.queryOwnerRoomRels(ownerRoomRelDto);
            //取得业主id
            ownerId = ownerRoomRelDtos.get(0).getOwnerId();
        } else if (payerObjType.equals("6666")) {
            OwnerCarDto ownerCarDto = new OwnerCarDto();
            ownerCarDto.setPsId(payerObjId);
            List<OwnerCarDto> ownerCarDtos = ownerCarInnerServiceSMO.queryOwnerCars(ownerCarDto);
            //取得业主id
            ownerId = ownerCarDtos.get(0).getOwnerId();
        }
        OwnerDto ownerDto = new OwnerDto();
        ownerDto.setOwnerId(ownerId);
        //1001 业主本人 1002 家庭成员
        ownerDto.setOwnerTypeCd("1001");
        //查询业主
        List<OwnerDto> ownerDtos = ownerInnerServiceSMO.queryOwners(ownerDto);
        //获得成员id
        String memberId = ownerDtos.get(0).getMemberId();
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        ownerAppUserDto.setMemberId(memberId);
        ownerAppUserDto.setAppType("WECHAT");
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMO.queryOwnerAppUsers(ownerAppUserDto);
        if (ownerAppUserDtos.size() > 0) {
            //获取openId
            String openId = ownerAppUserDtos.get(0).getOpenId();
            String url = sendMsgUrl + accessToken;
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(openId);
            data.setFirst(new Content("本次缴费已到账"));
            data.setKeyword1(new Content(paramIn.getString("payFeeRoom")));
            data.setKeyword2(new Content(paramIn.getString("feeTypeCdName")));
            data.setKeyword3(new Content(paramIn.getString("payFeeTime")));
            data.setKeyword4(new Content(paramIn.getString("receivedAmount") + "元"));
            data.setRemark(new Content("感谢您的使用,如有疑问请联系相关物业人员"));
            templateMessage.setData(data);
            String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL");
            templateMessage.setUrl(wechatUrl);
            logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(url, JSON.toJSONString(templateMessage), String.class);
            logger.info("微信模板返回内容:{}", responseEntity);
        } else {
            //获取业主手机号
            String tel = ownerDtos.get(0).getLink();
            //获取业主姓名
            String name = ownerDtos.get(0).getName();
            //获取费用类型
            String feeTypeCdName = paramIn.getString("feeTypeCdName");
            //获取费用开始时间
            String startTime = paramIn.getString("startTime");
            //获取费用结束时间
            String endTime = paramIn.getString("endTime");
            //获取缴费金额
            String receivedAmount = paramIn.getString("receivedAmount");
            //获取房屋号
            String payFeeRoom = paramIn.getString("payFeeRoom");
            DefaultProfile profile = DefaultProfile.getProfile(MappingCache.getValue(ALI_SMS_DOMAIN, "region"),
                    MappingCache.getValue(ALI_SMS_DOMAIN, "accessKeyId"),
                    MappingCache.getValue(ALI_SMS_DOMAIN, "accessSecret"));
            IAcsClient client = new DefaultAcsClient(profile);

            CommonRequest request = new CommonRequest();
            request.setSysMethod(MethodType.POST);
            request.setSysDomain("dysmsapi.aliyuncs.com");
            request.setSysVersion("2017-05-25");
            request.setSysAction("SendSms");
            request.putQueryParameter("RegionId", MappingCache.getValue(ALI_SMS_DOMAIN, "region"));
            request.putQueryParameter("PhoneNumbers", tel);
            request.putQueryParameter("SignName", MappingCache.getValue(ALI_SMS_DOMAIN, "signName"));
            //获取模板编码(SMS_207160078为缴费成功提示模板编码)
            String payFeeCode = MappingCache.getValue(ALI_SMS_DOMAIN, "PayFeeCode");
            String substring = "";
            if (!StringUtil.isEmpty(payFeeCode)) {
                substring = payFeeCode.substring(0, 4);
            }
            if (substring.equals("SMS_")) {
                request.putQueryParameter("TemplateCode", payFeeCode);
            }
            request.putQueryParameter("TemplateParam", "{\"user\":" + "\"" + name + "\"" + "," + "\"house\":" + "\"" + payFeeRoom + "\"" + "," + "\"feeType\":" + "\"" + feeTypeCdName + "\"" + "," + "\"startTime\":" + "\"" + startTime + "\"" + "," + "\"endTime\":" + "\"" + endTime + "\"" + "," + "\"mount\":" + "\"" + receivedAmount + "\"" + "}");
            try {
                CommonResponse response = client.getCommonResponse(request);
                logger.debug("发送短信：{}", response.getData());
            } catch (ServerException e) {
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            }
        }
    }
}
