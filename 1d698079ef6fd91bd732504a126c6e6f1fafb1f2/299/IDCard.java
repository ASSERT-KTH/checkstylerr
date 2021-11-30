package com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.idcard;

import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework.Product;


public class IDCard extends Product {
    private String owner;

    IDCard(String owner) {
        System.out.println("制作" + owner + "的ID卡。");
        this.owner = owner;
    }

    public void use() {
        System.out.println("使用" + owner + "的ID卡。");
    }

    public String getOwner() {
        return owner;
    }
}
