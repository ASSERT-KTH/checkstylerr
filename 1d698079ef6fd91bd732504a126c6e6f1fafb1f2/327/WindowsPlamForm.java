package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName WindowsPlamForm
 * @date 2019/8/14  16:24.
 * @Description
 */
public class WindowsPlamForm extends AbstractPlamForm {

    @Override
    public void play() {
        System.out.println("windows启动播放");
        mediaPlayInter.mediaPlay();
    }
}
