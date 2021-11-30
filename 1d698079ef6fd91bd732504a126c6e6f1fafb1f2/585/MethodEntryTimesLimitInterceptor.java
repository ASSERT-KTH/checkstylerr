package com.hlj.redis.contollertimes.aop;
import com.hlj.redis.contollertimes.annotation.EntryTimes;
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
 * @Desc: 控制每个用户访问Controller方法的次数
 * @Date:  2018/9/7 上午11:10.
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
    @Around("@annotation(com.hlj.redis.contollertimes.annotation.EntryTimes)")
    public Object aroundAdvice(ProceedingJoinPoint pjp) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        EntryTimes annotation = method.getAnnotation(EntryTimes.class);
        int times = annotation.value(); //自定义可有进入的次数，默认是1
        int userId = 1 ;
        String key = METHOD_CAN_ENTRY_TIMES_KEY + ":" + annotation.prefix() + ":" +  userId;
        // 没有整个方法使用一个redis链接，是为了防止方法执行时间过长一直占用redis链接。
        Long increment = getEntryTimes(key); //每次进入先再redis中增加1

        // 不放在try里面，不执行finally，防止重复进入删除上一个key
        if (increment > times){
            // 设置十秒钟超时，防止finally在系统崩溃或重启时没有执行造成用户不能操作。
            expireKey(key,10);
            return "错误";
        }
        Object retVal;
        try {
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
        Long expire = stringRedisTemplate.getExpire(key);
        if (expire == null || expire <= 0) {
            stringRedisTemplate.boundValueOps(key).expire(seconds, TimeUnit.SECONDS);
        }
    }
}
