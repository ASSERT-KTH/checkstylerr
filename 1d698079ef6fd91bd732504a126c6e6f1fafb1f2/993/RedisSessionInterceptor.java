package com.duodian.youhui.admin.config.interceptors;

import com.duodian.youhui.admin.config.CasConfig;
import com.duodian.youhui.admin.config.redis.CacheKey;
import com.duodian.youhui.admin.utils.IpUtil;
import com.duodian.youhui.admin.utils.JsonUtils;
import com.duodian.youhui.data.admin.SystemAdminLoginInfo;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

/**
 * @Desc: 将session 用户信息存储到redis中，进行持久化
 * @Date:  2018/9/12 上午11:54.
 */

@Component
@Slf4j
public class RedisSessionInterceptor implements HandlerInterceptor {


    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object o){

        if(stringRedisTemplate.opsForValue().get(CacheKey.LOGIN_SESSION+request.getSession().getId())==null) {
            if (CasConfig.RemoteUserUtil.hasLogin()) {
                SystemAdminLoginInfo systemAdminLoginInfo = new SystemAdminLoginInfo();
                systemAdminLoginInfo.setAdminId(Long.valueOf(CasConfig.RemoteUserUtil.getRemoteUserId().toString())).
                        setAdminName(CasConfig.RemoteUserUtil.getRemoteUserName()).
                        setEmail(CasConfig.RemoteUserUtil.getRemoteUserAccount()).
                        setSuperLogin(CasConfig.RemoteUserUtil.getRemoteUserSuper());
                stringRedisTemplate.opsForValue().set(CacheKey.LOGIN_SESSION + request.getSession().getId(), JSONObject.fromObject(systemAdminLoginInfo).toString(), 1L, TimeUnit.DAYS);

            }
        }else { //防止用户登录信息不一致
            Long remoteUserId = CasConfig.RemoteUserUtil.getRemoteUserId();
            if (remoteUserId != null) {
                SystemAdminLoginInfo systemAdmin = JsonUtils.toObject(stringRedisTemplate.opsForValue().get(CacheKey.LOGIN_SESSION+request.getSession().getId()),SystemAdminLoginInfo.class );
                if (remoteUserId.compareTo(systemAdmin.getAdminId()) != 0) {
                    SystemAdminLoginInfo systemAdminLoginInfo = new SystemAdminLoginInfo();
                    systemAdminLoginInfo.setAdminId(Long.valueOf(CasConfig.RemoteUserUtil.getRemoteUserId().toString())).
                            setAdminName(CasConfig.RemoteUserUtil.getRemoteUserName()).
                            setEmail(CasConfig.RemoteUserUtil.getRemoteUserAccount()).
                            setSuperLogin(CasConfig.RemoteUserUtil.getRemoteUserSuper());
                    stringRedisTemplate.opsForValue().set(CacheKey.LOGIN_SESSION + request.getSession().getId(), JSONObject.fromObject(systemAdminLoginInfo).toString(), 1L, TimeUnit.DAYS);

                }
            }
        }

        log.info("admin[{}],[{}],[{}];请求地址:[{}];访问ip:[{}]", CasConfig.RemoteUserUtil.getRemoteUserId(),CasConfig.RemoteUserUtil.getRemoteUserAccount(),CasConfig.RemoteUserUtil.getRemoteUserName(),request.getRequestURL(), IpUtil.getIp());
        return  true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

}
