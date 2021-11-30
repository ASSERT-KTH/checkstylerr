package com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample;

import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework.Factory;
import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.framework.Product;
import com.hlj.moudle.design.D02_交给子类.D04_FactoryMthod模式.Sample.idcard.IDCardFactory;

public class Main {
    public static void main(String[] args) {
        Factory factory = new IDCardFactory();
        Product card1 = factory.create("小明");
        Product card2 = factory.create("小红");
        Product card3 = factory.create("小刚");
        card1.use();
        card2.use();
        card3.use();
    }
}
