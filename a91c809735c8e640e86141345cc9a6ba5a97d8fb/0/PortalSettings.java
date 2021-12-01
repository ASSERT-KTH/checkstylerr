package com.ctrip.apollo.portal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ctrip.apollo.core.enums.Env;
import com.ctrip.apollo.portal.api.AdminServiceAPI;

@Component
public class PortalSettings {

  private Logger logger = LoggerFactory.getLogger(PortalSettings.class);

  private static final int HEALTH_CHECK_INTERVAL = 5000;

  @Value("#{'${apollo.portal.env}'.split(',')}")
  private List<String> allStrEnvs;

  @Autowired
  ApplicationContext applicationContext;

  private List<Env> allEnvs = new ArrayList<Env>();

  private volatile boolean updatedFromLastHealthCheck = true;

  //for cache
  private List<Env> activeEnvs = new LinkedList<>();

  //mark env up or down
  private Map<Env, Boolean> envStatusMark = new ConcurrentHashMap<>();

  private ScheduledExecutorService healthCheckService;

  private Lock lock = new ReentrantLock();

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
    if (updatedFromLastHealthCheck) {
      lock.lock();
      //maybe refresh many times but not create a bad impression.
      activeEnvs = refreshActiveEnvs();
      lock.unlock();
    }
    return activeEnvs;
  }

  private List<Env> refreshActiveEnvs() {
    List<Env> envs = new LinkedList<>();
    for (Env env : allEnvs) {
      if (envStatusMark.get(env)) {
        envs.add(env);
      }
    }
    logger.info("refresh active envs");
    return envs;
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
      logger.info("admin server health check start...");
      boolean hasUpdateStatus = false;

      for (Env env : allEnvs) {
        try {
          if (isUp(env)) {
            //revive
            if (!envStatusMark.get(env)) {
              envStatusMark.put(env, true);
              healthCheckFailCnt.put(env, 0l);
              hasUpdateStatus = true;
              logger.info("env up again [env:{}]", env);
            }
          } else {
            //maybe meta server up but admin server down
            hasUpdateStatus = handleEnvDown(env);
          }

        } catch (Exception e) {
          //maybe meta server down
          logger.warn("health check fail. [env:{}]", env, e.getMessage());
          hasUpdateStatus = handleEnvDown(env);
        }
      }

      if (!hasUpdateStatus) {
        logger.info("admin server health check OK");
      }
      updatedFromLastHealthCheck = hasUpdateStatus;
    }

    private boolean isUp(Env env) {
      Health health = healthAPI.health(env);
      return "UP".equals(health.getStatus().getCode());
    }

    private boolean handleEnvDown(Env env) {
      long failCnt = healthCheckFailCnt.get(env);
      healthCheckFailCnt.put(env, ++failCnt);

      if (envStatusMark.get(env) && failCnt >= ENV_DIED_THREADHOLD){
        envStatusMark.put(env, false);
        logger.error("env turn to down [env:{}]", env);
        return true;
      }else {
        logger.warn("[env:{}] down yet.", env);
        return false;
      }
    }

  }
}
