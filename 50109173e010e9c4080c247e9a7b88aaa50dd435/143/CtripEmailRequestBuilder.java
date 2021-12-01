package com.ctrip.framework.apollo.portal.spi.ctrip;

import com.ctrip.framework.apollo.portal.entity.bo.Email;
import com.ctrip.framework.apollo.tracer.Tracer;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

public class CtripEmailRequestBuilder {

  private static final Logger logger = LoggerFactory.getLogger(CtripEmailRequestBuilder.class);

  private static Class sendEmailRequestClazz;
  private static Method setBodyContent;
  private static Method setRecipient;
  private static Method setSendCode;
  private static Method setBodyTemplateID;
  private static Method setSender;
  private static Method setSubject;
  private static Method setIsBodyHtml;
  private static Method setCharset;
  private static Method setExpiredTime;
  private static Method setAppID;

  @Value("${ctrip.appid}")
  private int appId;
  //send code & template id. apply from ewatch
  @Value("${ctrip.email.send.code}")
  private String sendCode;
  @Value("${ctrip.email.template.id}")
  private int templateId;
  //email retention time in email server queue.TimeUnit: hour
  @Value("${ctrip.email.survival.duration}")
  private int survivalDuration;

  private Calendar expiredTime;

  @PostConstruct
  public void init() {
    try {
      sendEmailRequestClazz = Class.forName("com.ctrip.framework.apolloctripservice.emailservice.SendEmailRequest");

      setSendCode = sendEmailRequestClazz.getMethod("setSendCode", String.class);
      setBodyTemplateID = sendEmailRequestClazz.getMethod("setBodyTemplateID", Integer.class);
      setSender = sendEmailRequestClazz.getMethod("setSender", String.class);
      setBodyContent = sendEmailRequestClazz.getMethod("setBodyContent", String.class);
      setRecipient = sendEmailRequestClazz.getMethod("setRecipient", List.class);
      setSubject = sendEmailRequestClazz.getMethod("setSubject", String.class);
      setIsBodyHtml = sendEmailRequestClazz.getMethod("setIsBodyHtml", Boolean.class);
      setCharset = sendEmailRequestClazz.getMethod("setCharset", String.class);
      setExpiredTime = sendEmailRequestClazz.getMethod("setExpiredTime", Calendar.class);
      setAppID = sendEmailRequestClazz.getMethod("setAppID", Integer.class);

      expiredTime = calExpiredTime();

    } catch (Throwable e) {
      logger.error("init email request build failed", e);
      Tracer.logError("init email request build failed", e);
    }
  }


  public Object buildEmailRequest(Email email) throws Exception {

    Object emailRequest = createBasicEmailRequest();

    setSender.invoke(emailRequest, email.getSenderEmailAddress());
    setSubject.invoke(emailRequest, email.getSubject());
    setBodyContent.invoke(emailRequest, email.getBody());
    setRecipient.invoke(emailRequest, email.getRecipients());

    return emailRequest;
  }

  private Object createBasicEmailRequest() throws Exception {
    Object request = sendEmailRequestClazz.newInstance();

    setSendCode.invoke(request, sendCode);
    setBodyTemplateID.invoke(request, templateId);
    setIsBodyHtml.invoke(request, true);
    setCharset.invoke(request, "UTF-8");
    setExpiredTime.invoke(request, expiredTime);
    setAppID.invoke(request, appId);

    return request;
  }


  private Calendar calExpiredTime() {

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(DateUtils.addHours(new Date(), survivalDuration));

    return calendar;
  }


}
