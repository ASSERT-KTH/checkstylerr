package com.ctrip.apollo.portal;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ctrip.apollo.Apollo.Env;

@Component
public class PortalSettings {

  @Value("#{'${apollo.portal.env}'.split(',')}")
  private List<String> env;

  private List<Env> envs = new ArrayList<Env>();

  @PostConstruct
  private void postConstruct() {
    for (String e : env) {
      envs.add(Env.valueOf(e.toUpperCase()));
    }
  }

  public List<Env> getEnvs() {
    return envs;
  }
}
