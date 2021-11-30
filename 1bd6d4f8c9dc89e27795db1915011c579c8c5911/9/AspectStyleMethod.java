package com.healerjean.proj.config.aspect;

import com.healerjean.proj.dto.Demo.DemoDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.config.AspectEntry;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect //当前类标识为一个切面供容器读取  ,、@Aspect放在类头上，把这个类作为一个切面。
@Order(2) // 控制多个Aspect的执行顺序，越小越先执行
@Slf4j
public class AspectStyleMethod {

    /**
     * @Pointcut 放在方法头上，定义一个可被别的方法引用的切入点表达式
     */
    @Pointcut("execution(* com.healerjean.proj.service.*Service.*(..))")
    private void anyMethod() {
    }

    /**
     * @Before 标识一个前置增强方法
     */
    @Before("anyMethod()")
    public void before() {
        log.info("@Before");
    }

    /**
     * @After： final增强，不管是抛出异常或者正常退出都会执行
     */
    @After("anyMethod()")
    public void after() {
        log.info("@After");
    }

    /**
     * @AfterReturning ：后置增强，方法正常退出时执行（有了异常就不会执行）
     */
    @AfterReturning("anyMethod()")
    public void afterReturning() {
        log.info("@AfterReturning");
    }


    /**
     * @AfterThrowing： 异常抛出增强，（有了异常才会执行，否则不能够执行（在around异常处理之前执行））
     */
    @AfterThrowing("anyMethod()")
    public void afterThrowing() {
        log.info(" @AfterThrowing");
    }


    /**
     * 正常调用/有异常不捕获
     *
     * @Around： 环绕增强
     */
    // @Around("anyMethod()")
    // public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
    //     log.info("@Around pjp.proceed() 准备执行");
    //     Object object = pjp.proceed();
    //     //下面的log.info不会执行，因为AspectStyleService中有了异常，所以不会到这一步
    //     log.info("@Around   pjp.proceed() 执行完毕");
    //     return object;
    // }

    /**
     * 异常捕获
     * @Around： 环绕增强
     */
    @Around("anyMethod()")
    public Object doBasicProfiling(ProceedingJoinPoint pjp) throws Throwable {
        log.info("@Around   pjp.proceed() 准备执行");
        try {
            Object object = pjp.proceed();
            //下面的log.info不会执行，因为AspectStyleService中有了异常，所以不会到这一步
            log.info("@Around   pjp.proceed() 正常执行完毕");
            return object;
        } catch (Exception e) {
            log.info("@Around   pjp.proceed() 出错：{}", e.getMessage());
            throw e;
        }
    }


}
