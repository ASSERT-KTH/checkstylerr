package com.ctrip.apollo.biz.datasource;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.ctrip.apollo.core.utils.StringUtils;

public class TitanCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    if (!StringUtils.isEmpty(context.getEnvironment().getProperty("titan.url"))) {
      return true;
    }
    return false;
  }

}
