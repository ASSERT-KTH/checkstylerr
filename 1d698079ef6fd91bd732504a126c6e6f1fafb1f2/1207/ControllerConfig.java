package com.hlj.ddkj.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

import javax.annotation.Resource;

@Slf4j
@ControllerAdvice
public class ControllerConfig {

    Logger logger = LoggerFactory.getLogger(ControllerConfig.class);
/*

    @Resource
    ResourceUrlProvider resourceUrlProvider;

    @ModelAttribute("urls")
    public ResourceUrlProvider urls() {
        return this.resourceUrlProvider;
    }
*/

/*    @ExceptionHandler(value = Throwable.class) //处理抛出的异常。一般都是抛出的哦
    @ResponseBody
    public Response<?> handler(Throwable t){
        log.error("系统错误", t);
        return Response.error();
    }*/

}
