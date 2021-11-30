package com.fintech.scf.service.system.sendMsg.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName SendMailDTO
 * @date 2019/5/10  9:40.
 * @Description
 */
@Data
public class SendMailDTO implements Serializable {


    /**
     * 发送邮件所属业务
     */
    private String type ;

    /**
     * 发送人邮箱
     */
    private String fromMail;

    /**
     * 接受者邮箱，设置数组，可以同时发送给多个人
     */
    private String[] receiveMails;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String content;

    /**
     * 附件
     */
    private Map<String,Object> attachment;

    /**
     * 链接中的图片
     */
    private Map<String,Object> imageAttachment;

    /**
     * success 发送成功 ，error 发送失败
     */
    private String status;
    /**
     * 发送状态信息 发送成功、异常信息
     */
    private String msg;
}
