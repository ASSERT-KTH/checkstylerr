package com.duodian.youhui.admin.config.anno.aop;

import com.duodian.youhui.admin.bean.ResponseBean;
import com.duodian.youhui.admin.config.ContextHolder;
import com.duodian.youhui.admin.config.anno.annotation.Token;
import com.duodian.youhui.admin.moudle.user.service.UserInfoService;
import com.duodian.youhui.admin.utils.AesUtils;
import com.duodian.youhui.enums.exception.ErrorCodeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * @author HealerJean
 */
@Aspect
@Component
@Order(0)
public class TokenInterceptor {

    @Resource
    private UserInfoService userInfoService;

    /**
     * 先取method的Token，如果没有再去class的Token，如果有使用method的Token
     * 从request获取token进行解密，获取userId和oid，如果解密失败，返回错误信息
     * 把userId和oid放入当前上下文
     *
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.duodian.youhui.admin.config.anno.annotation.Token) || @within(com.duodian.youhui.admin.config.anno.annotation.Token)")
    protected Object invoke(ProceedingJoinPoint point) throws Throwable {
        Token token = null;
        Signature signature = point.getSignature();
        if (signature instanceof MethodSignature) {
            Method method = ((MethodSignature) signature).getMethod();
            token = method.getAnnotation(Token.class);
        }
        if (token == null) {
            token = point.getTarget().getClass().getAnnotation(Token.class);
        }
        if (token == null || !token.check()) {
            return point.proceed();
        }
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String tokenParam = request.getParameter("token");
        // String tokenParam = request.getHeader("anno");
        if (tokenParam == null) {
            return ResponseBean.buildFailure(ErrorCodeEnum.token认证失败)  ;
        }

        try {
            String decrip =  AesUtils.LoginDecrypt(tokenParam) ;
            if (decrip == null) {
                return ResponseBean.buildFailure(ErrorCodeEnum.token认证失败)  ;
            }
            Long userId = Long.valueOf( decrip);
            //检验redis中是否还存在，并比较相等
            if (!userInfoService.checkAppToken(userId, tokenParam)) {
                return ResponseBean.buildFailure(ErrorCodeEnum.token认证失败) ;
            }
            ContextHolder.setUserId(userId);
            return point.proceed();
        } catch (Exception e) {
            throw e;
        } finally {
            ContextHolder.clearUserId();
        }
    }

}
