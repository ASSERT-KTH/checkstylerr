package com.hlj.many.datasourse.data;

/**
 * @Description  自定义注解
 * @Date   2018/4/24 下午6:09.
 */

public enum DataSource {

    ONE("one"),
    TWO("two"),
    ;

    private String name;

    DataSource(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

}
