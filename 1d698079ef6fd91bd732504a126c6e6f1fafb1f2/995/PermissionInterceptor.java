package com.duodian.youhui.admin.config.interceptors;
import com.duodian.youhui.admin.config.CasConfig;
import com.duodian.youhui.admin.utils.IpUtil;
import com.duodian.youhui.dao.db.admin.SysAdminUserRepository;
import com.duodian.youhui.entity.db.admin.SysAdminUser;
import com.duodian.youhui.enums.EnumDelete;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/13
 * version：1.0.0
 */
@Component
@Slf4j
//只有确定的url才能进入拦截器 和/** 一样，只有不确定的url才会进入/**
public class PermissionInterceptor implements HandlerInterceptor {


    @Resource
    private SysAdminUserRepository sysAdminUserPermissionRepository;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {

       try {
            Long adminId = CasConfig.RemoteUserUtil.getRemoteUserId();
            List<SysAdminUser> sysAdminUserPermissions = sysAdminUserPermissionRepository.findByStatus(EnumDelete.可用.status);
            for(SysAdminUser sysAdminUserPermission :sysAdminUserPermissions){
                if (sysAdminUserPermission.getAdminId().compareTo(adminId)==0){
                    return  true;
                }
            }
            log.error("admin[{}],[{}],[{}];请求地址:[{}];访问ip:[{}]", CasConfig.RemoteUserUtil.getRemoteUserId(),CasConfig.RemoteUserUtil.getRemoteUserAccount(),CasConfig.RemoteUserUtil.getRemoteUserName(),httpServletRequest.getRequestURL(), IpUtil.getIp());
            //没有权限登录
            nopermission(httpServletRequest,httpServletResponse);
            return false;
        }catch (Exception e){
        }
        return  true;


    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }


    private void nopermission(HttpServletRequest request,HttpServletResponse response) throws IOException{
        String ajax = request.getHeader("X-Requested-With");
        if (StringUtils.equals(ajax,"XMLHttpRequest")){
            response.addHeader("nopermission","true");
        } else {
            response.sendRedirect("/duodian/public/nopermission"); //这里要设置拦截不拦截，否则会一直重定向
        }
    }


}
