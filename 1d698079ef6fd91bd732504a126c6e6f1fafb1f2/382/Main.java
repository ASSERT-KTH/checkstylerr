package com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D01_使用继承的适配器;

public class Main {
    public static void main(String[] args) {


        Print p = new AdapterPrintBanner("Hello");

        p.printWeak();

        p.printStrong();

    }
}
