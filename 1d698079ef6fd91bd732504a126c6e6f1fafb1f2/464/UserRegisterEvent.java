package com.hlj.applicationevent.ApplicationEvent;

import com.hlj.applicationevent.ApplicationEvent.Bean.UserBean;
import org.springframework.context.ApplicationEvent;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午5:06.
 *
 *ApplicationEvent以及Listener是Spring为我们提供的一个事件监听、订阅的实现
 * 内部实现原理是观察者设计模式，设计初衷也是为了系统业务逻辑之间的解耦，提高可扩展性以及可维护性。
 * 事件发布者并不需要考虑谁去监听，监听具体的实现内容是什么，发布者的工作只是为了发布事件而已。
 */
public class UserRegisterEvent extends ApplicationEvent {

    //注册用户对象
    private UserBean user;
    private  String other;

    /**
     * 重写构造函数
     * @param source 发生事件的对象,在那个service中发送事件
     * @param user 注册用户对象,other也是，other是我自己加上的
     *  其中source参数指的是发生事件的对象，一般我们在发布事件时使用的是this关键字代替本类对象，
     *             而user参数是我们自定义的注册用户对象，该对象可以在监听内被获取。
     */
    public UserRegisterEvent(Object source,UserBean user,String other) {
        super(source);
        this.user = user;
        this.other = other;

    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}