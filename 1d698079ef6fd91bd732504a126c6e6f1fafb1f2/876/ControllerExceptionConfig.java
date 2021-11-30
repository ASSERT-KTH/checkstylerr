package com.hlj.config;

import com.hlj.utils.ExceptionLogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.resource.ResourceUrlProvider;


@Slf4j
@ControllerAdvice
public class ControllerExceptionConfig {

    //配合application中的配置使用哦
    @ExceptionHandler(value = Throwable.class) //处理抛出的异常。一般都是抛出的哦
    @ResponseBody
    public String handler(Throwable t){
        ExceptionLogUtils.log(t,this.getClass());
        return  "SYSTEM ERROR/404/403/等）";
    }


}
