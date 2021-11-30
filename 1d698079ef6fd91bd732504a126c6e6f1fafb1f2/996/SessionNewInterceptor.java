package com.duodian.admore.config.interceptors;


import com.duodian.admore.admin.login.service.LoginService;
import com.duodian.admore.config.CasConfig;
import com.duodian.admore.constants.AppConstants;
import com.duodian.admore.entity.db.admin.SysAdminUser;
import com.duodian.admore.entity.db.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/13
 * version：1.0.0
 */
@Component
public class SessionNewInterceptor  implements HandlerInterceptor {

    private Logger logger = LoggerFactory.getLogger(SessionNewInterceptor.class);

    @Resource
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        if (httpServletRequest.getRequestURI().endsWith("/error")){
            return true;
        }
        HttpSession session = httpServletRequest.getSession();
        if (session.getAttribute(AppConstants.SESSION_ADMIN_USER) == null) {
            Long remoteUserId = CasConfig.RemoteUserUtil.getRemoteUserId();
            if (remoteUserId != null){
                SysAdminUser adminUser = loginService.getSysAdminUser(remoteUserId);
                session.setAttribute(AppConstants.SESSION_ADMIN_USER, adminUser);
                session.setAttribute(AppConstants.SESSION_CUSTOMER_COUNT,loginService.countUserByAdmId(remoteUserId));
            }
        } else {
            Long remoteUserId = CasConfig.RemoteUserUtil.getRemoteUserId();
            if (remoteUserId != null) {
                SysAdminUser adminUser = (SysAdminUser)session.getAttribute(AppConstants.SESSION_ADMIN_USER);
                if (adminUser.getId().compareTo(remoteUserId) != 0) { //一般肯定的是相等的，毫无疑问啊
                    adminUser = loginService.getSysAdminUser(remoteUserId);
                    session.setAttribute(AppConstants.SESSION_ADMIN_USER,adminUser);
                    session.setAttribute(AppConstants.SESSION_CUSTOMER_COUNT, loginService.countUserByAdmId(remoteUserId));
                }
            }
        }

        this.logRequest(httpServletRequest,session);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }

    private void logRequest(HttpServletRequest request,HttpSession session){
        try {
            logger.info("admin[{}],[{}],[{}];请求地址:[{}]",CasConfig.RemoteUserUtil.getRemoteUserId(),CasConfig.RemoteUserUtil.getRemoteUserAccount(),CasConfig.RemoteUserUtil.getRemoteUserName(),request.getRequestURL());
        } catch (Exception e) {
            // ignore
        }
    }

}
