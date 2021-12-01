package com.ctrip.framework.apollo.portal.components.emailbuilder;


import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.entity.bo.ReleaseHistoryBO;

import org.springframework.stereotype.Component;


@Component
public class MergeEmailBuilder extends ConfigPublishEmailBuilder {

  private static final String EMAIL_SUBJECT = "[Apollo] 全量发布";


  @Override
  protected String subject() {
    return EMAIL_SUBJECT;
  }

  @Override
  protected String emailContent(Env env, ReleaseHistoryBO releaseHistory) {
    String template = getReleaseTemplate();
    return renderEmailCommonContent(template, env, releaseHistory);
  }
}
