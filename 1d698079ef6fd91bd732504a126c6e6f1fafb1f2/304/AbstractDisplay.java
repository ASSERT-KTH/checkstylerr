package com.hlj.moudle.design.D02_交给子类.D03_TeampleMethod模式.TemplateMethod.Sample;

public abstract class AbstractDisplay { // 抽象类AbstractDisplay
    public abstract void open();        // 交给子类去实现的抽象方法(1) open
    public abstract void print();       // 交给子类去实现的抽象方法(2) run
    public abstract void close();       // 交给子类去实现的抽象方法(3) close
    public final void display() {       // 本抽象类中实现的display方法
        open();                         // 首先打开…
        for (int i = 0; i < 5; i++) {   // 循环调用5次print
            print();
        }
        close();                        // …最后关闭。这就是display方法所实现的功能
    }
}
