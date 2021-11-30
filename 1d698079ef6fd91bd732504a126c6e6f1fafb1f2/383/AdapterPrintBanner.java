package com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D02_使用委托的适配器;

import com.hlj.moudle.design.D01_适应设计模式.D02_Adapter模式.D00_适配前.Banner;

/**
 * 适配器，委托实现，聚合
 */
public class AdapterPrintBanner implements Print {

    private Banner banner;

    public AdapterPrintBanner(String string) {
        this.banner = new Banner(string);
    }

    @Override
    public void printWeak() {
        banner.showWithParen();
    }

    @Override
    public void printStrong() {
        banner.showWithAster();
    }
}
