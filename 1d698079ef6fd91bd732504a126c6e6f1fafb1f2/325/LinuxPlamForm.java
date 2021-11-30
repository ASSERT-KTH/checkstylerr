package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName LinuxPlamForm
 * @date 2019/8/14  16:22.
 * @Description
 */
public class LinuxPlamForm extends AbstractPlamForm {

    @Override
    public void play() {
        System.out.println("Linux启动播放");
        mediaPlayInter.mediaPlay();
    }
}
