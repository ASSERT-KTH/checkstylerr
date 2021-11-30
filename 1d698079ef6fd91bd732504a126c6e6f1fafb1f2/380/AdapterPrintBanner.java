package com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D01_使用继承的适配器;


import com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D00_适配前.Banner;

/**
 * 适配器角色
 */
public class AdapterPrintBanner extends Banner implements Print {

    public AdapterPrintBanner(String string) {
        super(string);
    }

    @Override
    public void printWeak() {
        showWithParen();
    }

    @Override
    public void printStrong() {
        showWithAster();
    }
}
