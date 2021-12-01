package com.java110.job.task.wechat;

import com.alibaba.fastjson.JSON;
import com.java110.core.factory.WechatFactory;
import com.java110.dto.RoomDto;
import com.java110.dto.community.CommunityDto;
import com.java110.dto.notice.NoticeDto;
import com.java110.dto.owner.OwnerAppUserDto;
import com.java110.dto.owner.OwnerRoomRelDto;
import com.java110.dto.smallWeChat.SmallWeChatDto;
import com.java110.dto.smallWechatAttr.SmallWechatAttrDto;
import com.java110.dto.task.TaskDto;
import com.java110.entity.wechat.Content;
import com.java110.entity.wechat.Data;
import com.java110.entity.wechat.PropertyFeeTemplateMessage;
import com.java110.intf.community.INoticeInnerServiceSMO;
import com.java110.intf.community.IRoomInnerServiceSMO;
import com.java110.intf.store.ISmallWeChatInnerServiceSMO;
import com.java110.intf.store.ISmallWechatAttrInnerServiceSMO;
import com.java110.intf.user.IOwnerAppUserInnerServiceSMO;
import com.java110.intf.user.IOwnerRoomRelInnerServiceSMO;
import com.java110.job.quartz.TaskSystemQuartz;
import com.java110.utils.cache.MappingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @program: MicroCommunity
 * @description: 微信公众号主动推送信息
 * @author: wuxw
 * @create: 2020-06-15 13:35
 **/
@Component
public class WeChatPushMessageTemplate extends TaskSystemQuartz {

    private static Logger logger = LoggerFactory.getLogger(WeChatPushMessageTemplate.class);

    @Autowired
    private INoticeInnerServiceSMO noticeInnerServiceSMOImpl;

    @Autowired
    private ISmallWeChatInnerServiceSMO smallWeChatInnerServiceSMOImpl;

    @Autowired
    private ISmallWechatAttrInnerServiceSMO smallWechatAttrInnerServiceSMOImpl;

    @Autowired
    private IOwnerAppUserInnerServiceSMO ownerAppUserInnerServiceSMOImpl;

    @Autowired
    private IRoomInnerServiceSMO roomInnerServiceSMOImpl;

    @Autowired
    private IOwnerRoomRelInnerServiceSMO ownerRoomRelInnerServiceSMOImpl;

    @Autowired
    private RestTemplate outRestTemplate;

    //模板信息推送地址
    private static String sendMsgUrl = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token=";


    @Override
    protected void process(TaskDto taskDto) {
        logger.debug("开始执行微信模板信息推送" + taskDto.toString());

        // 获取小区
        List<CommunityDto> communityDtos = getAllCommunity();

        for (CommunityDto communityDto : communityDtos) {
            try {
                publishMsg(taskDto, communityDto);
            } catch (Exception e) {
                logger.error("推送消息失败", e);
            }
        }
    }

    private void publishMsg(TaskDto taskDto, CommunityDto communityDto) throws Exception {

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
        smallWechatAttrDto.setSpecCd(SmallWechatAttrDto.SPEC_CD_WECHAT_TEMPLATE);
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

        //
        NoticeDto noticeDto = new NoticeDto();
        noticeDto.setCommunityId(communityDto.getCommunityId());
        noticeDto.setPage(1);
        noticeDto.setRow(50);
        noticeDto.setState(NoticeDto.STATE_WAIT);
        noticeDto.setNoticeTypeCd(NoticeDto.NOTICE_TYPE_OWNER_WECHAT);
        List<NoticeDto> noticeDtos = noticeInnerServiceSMOImpl.queryNotices(noticeDto);

        if (noticeDtos == null || noticeDtos.size() < 1) {
            return;
        }

        for (NoticeDto tmpNotice : noticeDtos) {
            doSentWechat(tmpNotice, templateId, accessToken);
        }

    }

    private void doSentWechat(NoticeDto noticeDto, String templateId, String accessToken) {

        String objType = noticeDto.getObjType();

        switch (objType) {
            case NoticeDto.OBJ_TYPE_COMMUNITY:
                sendAllOwner(noticeDto, templateId, accessToken);
                break;
            case NoticeDto.OBJ_TYPE_FLOOR:
                sendFloorOwner(noticeDto, templateId, accessToken);
                break;
            case NoticeDto.OBJ_TYPE_UNIT:
                sendUnitOwner(noticeDto, templateId, accessToken);
                break;
            case NoticeDto.OBJ_TYPE_ROOM:
                sendRoomOwner(noticeDto, templateId, accessToken);
                break;
        }

    }

    private void sendFloorOwner(NoticeDto noticeDto, String templateId, String accessToken) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setFloorId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken);
        }
    }

    private void sendUnitOwner(NoticeDto noticeDto, String templateId, String accessToken) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setUnitId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken);
        }
    }

    private void sendRoomOwner(NoticeDto noticeDto, String templateId, String accessToken) {

        RoomDto roomDto = new RoomDto();
        roomDto.setCommunityId(noticeDto.getCommunityId());
        roomDto.setRoomId(noticeDto.getObjId());
        List<RoomDto> roomDtos = roomInnerServiceSMOImpl.queryRooms(roomDto);

        for (RoomDto tmpRoomDto : roomDtos) {
            if (!RoomDto.STATE_SELL.equals(tmpRoomDto.getState())) {
                continue;
            }
            OwnerRoomRelDto ownerRoomRelDto = new OwnerRoomRelDto();
            ownerRoomRelDto.setRoomId(tmpRoomDto.getRoomId());
            List<OwnerRoomRelDto> ownerRoomRelDtos = ownerRoomRelInnerServiceSMOImpl.queryOwnerRoomRels(ownerRoomRelDto);

            if (ownerRoomRelDtos == null || ownerRoomRelDtos.size() < 1) {
                continue;
            }
            OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
            ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
            ownerAppUserDto.setMemberId(ownerRoomRelDtos.get(0).getOwnerId());
            List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
            doSend(ownerAppUserDtos, noticeDto, templateId, accessToken);
        }
    }

    private void sendAllOwner(NoticeDto noticeDto, String templateId, String accessToken) {
        OwnerAppUserDto ownerAppUserDto = new OwnerAppUserDto();
        ownerAppUserDto.setAppType(OwnerAppUserDto.APP_TYPE_WECHAT);
        List<OwnerAppUserDto> ownerAppUserDtos = ownerAppUserInnerServiceSMOImpl.queryOwnerAppUsers(ownerAppUserDto);
        doSend(ownerAppUserDtos, noticeDto, templateId, accessToken);
    }

    private void doSend(List<OwnerAppUserDto> ownerAppUserDtos, NoticeDto noticeDto, String templateId, String accessToken) {
        String wechatUrl = MappingCache.getValue("OWNER_WECHAT_URL") + "/#/pages/notice/detail/detail?noticeId=";
        for (OwnerAppUserDto appUserDto : ownerAppUserDtos) {
            Data data = new Data();
            PropertyFeeTemplateMessage templateMessage = new PropertyFeeTemplateMessage();
            templateMessage.setTemplate_id(templateId);
            templateMessage.setTouser(appUserDto.getOpenId());
            data.setFirst(new Content(noticeDto.getTitle()));
            data.setKeyword1(new Content(noticeDto.getTitle()));
            data.setKeyword2(new Content(noticeDto.getStartTime()));
            data.setKeyword3(new Content(noticeDto.getContext()));
            data.setRemark(new Content("如有疑问请联系相关物业人员"));
            templateMessage.setData(data);
            templateMessage.setUrl(wechatUrl + noticeDto.getNoticeId());
            logger.info("发送模板消息内容:{}", JSON.toJSONString(templateMessage));
            ResponseEntity<String> responseEntity = outRestTemplate.postForEntity(sendMsgUrl + accessToken, JSON.toJSONString(templateMessage), String.class);
            logger.info("微信模板返回内容:{}", responseEntity);
        }
    }

}
