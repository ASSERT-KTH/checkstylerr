package com.hlj.applicationevent.ApplicationEvent.Listener;


import com.hlj.applicationevent.ApplicationEvent.Bean.UserBean;
import com.hlj.applicationevent.ApplicationEvent.UserRegisterEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午5:15.
 */
@Component
public class AnnotationRegisterListener {

    /**
     * 注册监听实现方法
     * @param userRegisterEvent 用户注册事件
     */
    @EventListener
    public void register(UserRegisterEvent userRegisterEvent)
    {
        //获取注册用户对象
        UserBean user = userRegisterEvent.getUser();
        String other = userRegisterEvent.getOther();

        //../省略逻辑

        //输出注册用户信息
        System.out.println("@EventListener注册信息，用户名："+user.getName()+"，密码："+user.getPassword());
        System.out.println("@EventListener注册信息，other："+other);

    }
}