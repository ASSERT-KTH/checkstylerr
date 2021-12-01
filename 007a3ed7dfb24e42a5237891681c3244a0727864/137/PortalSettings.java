package com.ctrip.framework.apollo.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.portal.api.AdminServiceAPI;

@Component
public class PortalSettings {

  private Logger logger = LoggerFactory.getLogger(PortalSettings.class);

  private static final int HEALTH_CHECK_INTERVAL = 5000;

  @Value("#{'${apollo.portal.env}'.split(',')}")
  private List<String> allStrEnvs;

  @Autowired
  ApplicationContext applicationContext;

  private List<Env> allEnvs = new ArrayList<Env>();

  private List<Env> activeEnvs;

  //mark env up or down
  private Map<Env, Boolean> envStatusMark = new ConcurrentHashMap<>();

  private ScheduledExecutorService healthCheckService;

  @PostConstruct
  private void postConstruct() {
    //init origin config envs
    for (String e : allStrEnvs) {
      allEnvs.add(Env.valueOf(e.toUpperCase()));
    }

    for (Env env : allEnvs) {
      envStatusMark.put(env, true);
    }

    healthCheckService = Executors.newScheduledThreadPool(1);

    healthCheckService
        .scheduleWithFixedDelay(new HealthCheckTask(applicationContext), 1000, HEALTH_CHECK_INTERVAL,
                                TimeUnit.MILLISECONDS);

  }

  public List<Env> getActiveEnvs() {
    List<Env> activeEnvs = new LinkedList<>();
    for (Env env : allEnvs) {
      if (envStatusMark.get(env)) {
        activeEnvs.add(env);
      }
    }
    this.activeEnvs = activeEnvs;
    return activeEnvs;
  }

  public Env getFirstAliveEnv() {
    return activeEnvs.get(0);
  }


  class HealthCheckTask implements Runnable {

    private static final int ENV_DIED_THREADHOLD = 2;

    private Map<Env, Long> healthCheckFailCnt = new HashMap<>();

    private AdminServiceAPI.HealthAPI healthAPI;

    public HealthCheckTask(ApplicationContext context) {
      healthAPI = context.getBean(AdminServiceAPI.HealthAPI.class);
      for (Env env : allEnvs) {
        healthCheckFailCnt.put(env, 0l);
      }
    }

    public void run() {

      for (Env env : allEnvs) {
        try {
          if (isUp(env)) {
            //revive
            if (!envStatusMark.get(env)) {
              envStatusMark.put(env, true);
              healthCheckFailCnt.put(env, 0l);
              logger.info("env up again [env:{}]", env);
            }
          } else {
            //maybe meta server up but admin server down
            handleEnvDown(env);
          }

        } catch (Exception e) {
          //maybe meta server down
          logger.warn("health check fail. [env:{}]", env, e.getMessage());
          handleEnvDown(env);
        }
      }

    }

    private boolean isUp(Env env) {
      Health health = healthAPI.health(env);
      return "UP".equals(health.getStatus().getCode());
    }

    private void handleEnvDown(Env env) {
      long failCnt = healthCheckFailCnt.get(env);
      healthCheckFailCnt.put(env, ++failCnt);

      if (!envStatusMark.get(env)) {
        logger.warn("[env:{}] down yet.", env);
      } else {
        if (failCnt >= ENV_DIED_THREADHOLD) {
          envStatusMark.put(env, false);
          logger.error("env turn to down [env:{}]", env);
        } else {
          logger.warn("env health check fail first time. [env:{}]", env);
        }
      }

    }

  }
}
