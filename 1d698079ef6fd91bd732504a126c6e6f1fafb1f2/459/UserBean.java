package com.hlj.applicationevent.ApplicationEvent.Bean;

/**
 * @Description
 * @Author HealerJean
 * @Date 2018/3/29  下午5:12.
 */
public class UserBean {
    //用户名
    private String name;
    //密码
    private String password;

    public UserBean() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
