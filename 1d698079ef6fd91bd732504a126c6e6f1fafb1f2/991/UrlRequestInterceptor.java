package com.duodian.admore.advertiser.interceptor;

import com.duodian.admore.common.utils.AppSessionHelper;
import com.duodian.admore.dao.redis.RedisUrlRequestData;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 类描述：
 * 创建人： j.sh
 * 创建时间： 2016/9/13
 * version：1.0.0
 */
@Component
public class UrlRequestInterceptor implements HandlerInterceptor {

    @Resource
    private RedisUrlRequestData redisUrlRequestData;

    private Map<String,Long> urlFilter = new HashMap<>();

    public UrlRequestInterceptor() {
        super();
        urlFilter.put("/apps/plan/savePlan",10L);
        urlFilter.put("/play/plan/savePlan",10L);
    }

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o) throws Exception {
        String uri = httpServletRequest.getRequestURI();
        if (urlFilter.get(uri) != null){
            Long userId = AppSessionHelper.getRemoteUserId();
            Long count = redisUrlRequestData.increase(userId + uri,urlFilter.get(uri));
            if (count > 1){
                this.invalidate(httpServletRequest,httpServletResponse);
                return false;
            }
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

    }


    private void invalidate(HttpServletRequest request,HttpServletResponse response) throws IOException {
        String ajax = request.getHeader("X-Requested-With");
        if (StringUtils.equals(ajax,"XMLHttpRequest")) {
            //设置登陆超时header
            response.addHeader("resubmit","true");
        } else {
            response.sendRedirect("/public/resubmit");
        }
    }

}
