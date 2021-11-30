package com.duodian.admore.zhaobutong.aop;

import com.duodian.admore.zhaobutong.annotation.EntryTimes;
import com.duodian.admore.zhaobutong.bean.Code;
import com.duodian.admore.zhaobutong.bean.Response;
import com.duodian.admore.zhaobutong.util.AesUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * 控制每个用户访问Controller方法的次数
 * Created by fengchuanbo on 2017/5/25.
 */
@Aspect
@Component
public class MethodEntryTimesLimitInterceptor {

    private static final String METHOD_CAN_ENTRY_TIMES_KEY = "method:entry:times:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 需要有 EntryTimes 标注，并且第一个参数需要是 AuthUser才可以
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.duodian.admore.zhaobutong.annotation.EntryTimes)")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getParameter("token");
        String aes = AesUtils.LoginDecrypt(token);
        Long userId = Long.valueOf(aes.split("#")[0]);
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        EntryTimes annotation = method.getAnnotation(EntryTimes.class);
        int times = annotation.value();
        String key = METHOD_CAN_ENTRY_TIMES_KEY + ":" + annotation.prefix() + ":" +  userId;
        // 没有整个方法使用一个redis链接，是为了方式方法执行时间过长一直占用redis链接。
        Long increment = getEntryTimes(key);
        Object retVal;
        try {
            // 放在try里面，才能执行finally
            if (increment > times){
                // 设置十秒钟超时，防止finally在系统崩溃或重启时没有执行造成用户不能操作。
                expireKey(key,10);
                return Response.of(Code.ACTION_FREQUENT);
            }
            //调用核心逻辑
            retVal = pjp.proceed();
        }finally {
            deleteKey(key);
        }
        return retVal;
    }

    private Long getEntryTimes(String key){
        return stringRedisTemplate.opsForValue().increment(key,1);
    }

    private void deleteKey(String key){
        stringRedisTemplate.delete(key);
    }

    private void expireKey(String key, int seconds){
        stringRedisTemplate.boundValueOps(key).expire(seconds, TimeUnit.SECONDS);
    }
}
