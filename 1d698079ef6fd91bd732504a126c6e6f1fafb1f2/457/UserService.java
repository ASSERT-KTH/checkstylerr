package com.hlj.applicationevent.ApplicationEvent.service;


import com.hlj.applicationevent.ApplicationEvent.Bean.UserBean;
import com.hlj.applicationevent.ApplicationEvent.UserRegisterEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午5:13.
 * UserService内添加一个注册方法，该方法只是实现注册事件发布功能
 */
@Service
public class UserService
{
    @Autowired
    ApplicationContext applicationContext;

    /**
     * 用户注册方法
     * @param user
     */
    public void register(UserBean user)
    {
        //../省略其他逻辑

        //发布UserRegisterEvent事件
        applicationContext.publishEvent(new UserRegisterEvent(this,user,"HealerJean"));
    }
}