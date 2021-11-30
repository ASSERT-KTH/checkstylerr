package com.didispace.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

public class AccessFilter extends ZuulFilter  {

    private static Logger log = LoggerFactory.getLogger(AccessFilter.class);

    /**
     * 过滤器类型 这里定义为pre 意味着在请求路由之前执行
     * @return
     */
    @Override
    public String filterType() {
        return "pre";
    }

    /**
     * 过滤器执行顺序，当请求中存在多个过滤器时，需要根据这个返回值来依次执行
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * 判断过滤器是否需要被执行，这里返回，true 因此这个过滤器对所有的请求都会生效，
     *  实际操作中可以利用这里指定过滤器的有效范围
     * @return
     */
    @Override
    public boolean shouldFilter() {
        return true;
    }

    /**
     * 过滤器的具体逻辑
     * @return
     */
    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        log.info("send {} request to {}", request.getMethod(), request.getRequestURL().toString());

        Object accessToken = request.getParameter("accessToken");
        if(accessToken == null) {
            log.warn("access token is empty");
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(401);
            return null;
        }
        log.info("access token ok");
        return null;
    }

}
