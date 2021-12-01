package com.ctrip.framework.apollo.portal.components.emailbuilder;


import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;

import org.springframework.stereotype.Component;


@Component
public class RollbackEmailBuilder extends ConfigPublishEmailBuilder {

  private static final String EMAIL_SUBJECT = "[Apollo] 配置回滚";


  @Override
  protected String subject() {
    return EMAIL_SUBJECT;
  }

  @Override
  protected String emailContent(Env env, ReleaseHistoryBO releaseHistory) {
    String template = getRollbackTemplate();
    return renderEmailCommonContent(template, env, releaseHistory);
  }
}
