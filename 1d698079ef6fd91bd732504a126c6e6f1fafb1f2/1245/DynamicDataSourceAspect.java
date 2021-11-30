package com.hlj.many.datasourse.aop;


import com.hlj.many.datasourse.data.DataSource;
import com.hlj.many.datasourse.data.DynamicDataSourceContextHolder;
import com.hlj.many.datasourse.data.TargetDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Created by fengchuanbo on 2017/5/8.
 */
@Aspect
@Order(0)// 保证该AOP在@Transactional之前执行
@Component
@Slf4j
public class DynamicDataSourceAspect {

    @Around("@annotation(com.hlj.many.datasourse.data.TargetDataSource) || @within(com.hlj.many.datasourse.data.TargetDataSource)")
    public Object invoke(ProceedingJoinPoint point) throws Throwable {
        try {
            String dataSource = null;
            Signature signature = point.getSignature();
            // 默认使用方法上的数据源
            if (signature instanceof MethodSignature) {
                Method method = ((MethodSignature) signature).getMethod();
                TargetDataSource annotation = method.getAnnotation(TargetDataSource.class);
                if (annotation != null){
                    dataSource = annotation.value().getName();
                }
            }
            // 方法没有标注数据源，使用类标注的
            if (dataSource == null){
                Annotation annotation = signature.getDeclaringType().getAnnotation(TargetDataSource.class);
                if (annotation instanceof TargetDataSource){
                    dataSource = ((TargetDataSource)annotation).value().getName();
                }
            }
            // 如果都没有，则使用默认数据源
            if (dataSource == null){
                dataSource = DataSource.ONE.getName();
            }
            if (!DynamicDataSourceContextHolder.containsDataSource(dataSource)) {
                log.error("数据源[{}]不存在，使用默认数据源 > {}", dataSource, point.getSignature());
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Use DataSource : {} > {}", dataSource, point.getSignature());
                }
                DynamicDataSourceContextHolder.setDataSource(dataSource);
            }

            return point.proceed();
        }catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            DynamicDataSourceContextHolder.clearDataSource();
        }
    }

}
