package com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D00_适配前;


/**
 * 目前的实际情况
 */
public class Banner  {

    public String string;

    public Banner(String string) {
        this.string = string;
    }
    public void showWithParen() {
        System.out.println("(" + string + ")");
    }
    public void showWithAster() {
        System.out.println("*" + string + "*");
    }
}
