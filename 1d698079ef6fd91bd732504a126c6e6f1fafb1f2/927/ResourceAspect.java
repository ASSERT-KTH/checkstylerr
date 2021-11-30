//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.duodian.admore.plugin.resource.aop;

import com.duodian.admore.plugin.resource.annotation.ResourceFolder;
import java.lang.annotation.Annotation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ResourceAspect {
    public ResourceAspect() {
    }

    @Around("within(@org.springframework.stereotype.Controller *) && @annotation(com.duodian.admore.plugin.resource.annotation.ResourceAppend)")
    public Object afterProcess(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Annotation resourceFolder = proceedingJoinPoint.getSignature().getDeclaringType().getAnnotation(ResourceFolder.class);
        if (resourceFolder == null) {
            return proceedingJoinPoint.proceed();
        } else {
            String result = proceedingJoinPoint.proceed().toString();
            return result.startsWith("redirect:") ? result : ((ResourceFolder)resourceFolder).folder() + result;
        }
    }
}
