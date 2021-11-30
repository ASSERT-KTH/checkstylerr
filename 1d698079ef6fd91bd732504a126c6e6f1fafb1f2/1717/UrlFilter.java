package com.hlj.sso.client.one.filter;


import org.jasig.cas.client.authentication.UrlPatternMatcherStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlFilter implements UrlPatternMatcherStrategy {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean matches(String url) {
        logger.debug("访问路径：" + url);
        return url.contains("url.jsp");
    }

    @Override
    public void setPattern(String pattern) {

    }
}
