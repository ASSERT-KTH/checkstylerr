package com.healerjean.proj.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.config.AspectEntry;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class AspectUser {

    @Pointcut("execution(* com.healerjean.proj.service.*.*(..))")
    private void anyMethod() {
    }


    @Before("anyMethod()")
    public void before() {
    }

    @After("anyMethod()")
    public void after() {
    }

    @AfterReturning("anyMethod()")
    public void doAfter() {
    }

    @AfterThrowing("anyMethod()")
    public void doAfterThrow() {
    }

    @Around("anyMethod()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        try {
            Long stratTime = System.currentTimeMillis();
            log.info("Around--------方法执行--------开始时间：{}", stratTime);
            Object object = pjp.proceed();
            Long endTime = System.currentTimeMillis();
            log.info("Around--------方法执行--------结束时间：{}", endTime);
            return object;

            //如果说捕获到异常，会当上面的最终结果 @After 和@AfterThrowing 执行完毕，再执行下面的
        } catch (Exception e) {
            log.info("Around--------方法执行--------出现异常：", e);
        }
        return null;
    }
}
