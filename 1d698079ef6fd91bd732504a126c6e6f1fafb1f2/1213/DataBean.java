package com.hlj.ddkj.lombok;

import lombok.Data;

@Data
public class DataBean {

    String name;


    public static void main(String[] args) {
        DataBean entityBean = new DataBean();
        entityBean.setName("HealerJean");
        System.out.println(entityBean.getName());

    }
}
