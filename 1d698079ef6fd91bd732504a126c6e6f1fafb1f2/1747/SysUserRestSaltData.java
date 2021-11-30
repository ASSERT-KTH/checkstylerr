package com.hlj.sso.server.rest.BeanData;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

public class SysUserRestSaltData {

 

    //@JsonProperty @JsonProperty不仅仅是在序列化的时候有用，
    // 反序列化的时候也有用，比如有些接口返回的是json字符串，
    // 命名又不是标准的驼峰形式，在映射成对象的时候，
    // 将类的属性上加上@JsonProperty注解，里面写上返回的json串对应的名字
    @JsonProperty("id")
    private  String email;

    @JsonProperty("@class")
    //需要返回实现org.apereo.cas.authentication.principal.Principal的类名接口
    private String clazz = "org.apereo.cas.authentication.principal.SimplePrincipal";

    @JsonProperty("attributes")
    private Map<String, Object> attributes = new HashMap<>();

    @JsonIgnore
    @NotNull
    private String username;

    @JsonIgnore
    @NotNull
    private String password;

    @JsonIgnore
    //用户是否不可用
    private boolean disable = false;
    @JsonIgnore
    //用户是否过期
    private boolean expired = false;

    @JsonIgnore
    //是否锁定
    private boolean locked = false;

    @JsonIgnore
    private Long id;

    @JsonIgnore
    private String salt;


    @JsonIgnore
    private String address;

    @JsonIgnore
    private int age;



    public boolean isLocked() {
        return locked;
    }

    public SysUserRestSaltData setLocked(int locked) {
        if(locked==1){
            this.locked = true;
        }
        return this;
    }

    public boolean isDisable() {
        return disable;
    }

    public SysUserRestSaltData setDisable(int disable) {
        if(disable==1){
            this.disable = true;
        }
        return this;
    }

    public boolean isExpired() {
        return expired;
    }

    public SysUserRestSaltData setExpired(int expired) {
        if(expired==1){
            this.expired = true;
        }        return this;
    }

    public String getPassword() {
        return password;
    }

    public SysUserRestSaltData setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public SysUserRestSaltData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public SysUserRestSaltData setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
        return this;
    }


    public Long getId() {
        return id;
    }

    public SysUserRestSaltData setId(Long id) {
        this.id = id;
        return this;

    }

    public String getSalt() {
        return salt;
    }

    public SysUserRestSaltData setSalt(String salt) {
        this.salt = salt;
        return this;

    }

    public String getEmail() {
        return email;
    }

    public SysUserRestSaltData setEmail(String email) {
        this.email = email;
        return this;

    }

    public String getAddress() {
        return address;
    }

    public SysUserRestSaltData setAddress(String address) {
        this.address = address;
        return this;
    }

    public int getAge() {
        return age;
    }

    public SysUserRestSaltData setAge(int age) {
        this.age = age;
        return this;
    }


    @JsonIgnore
    public SysUserRestSaltData addAttribute(String key, Object val) {
        getAttributes().put(key, val);
        return this;
    }

}


