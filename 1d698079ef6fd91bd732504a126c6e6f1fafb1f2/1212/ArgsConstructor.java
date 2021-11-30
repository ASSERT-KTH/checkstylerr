package com.hlj.ddkj.lombok;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PROTECTED) //指定方法的封装
@NoArgsConstructor
public class ArgsConstructor {
    private int x;

    private String name;
    public static void main(String[] args) {
        //有参构造器
        ArgsConstructor argsConstructor = new ArgsConstructor(2,"HealerJean");
    }

}
