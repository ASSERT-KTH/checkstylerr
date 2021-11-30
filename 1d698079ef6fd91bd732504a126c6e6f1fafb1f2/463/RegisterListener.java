package com.hlj.applicationevent.ApplicationEvent.Listener;


import com.hlj.applicationevent.ApplicationEvent.Bean.UserBean;
import com.hlj.applicationevent.ApplicationEvent.UserRegisterEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午5:24.
 */
@Component
public class RegisterListener implements ApplicationListener<UserRegisterEvent>
{
    /**
     * 实现监听
     * @param userRegisterEvent
     */
    @Override
    public void onApplicationEvent(UserRegisterEvent userRegisterEvent) {
        //获取注册用户对象
        UserBean user = userRegisterEvent.getUser();

        //../省略逻辑

        //输出注册用户信息
        System.out.println("注册信息，用户名："+user.getName()+"，密码："+user.getPassword());
    }
}