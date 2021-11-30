package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/14  16:25.
 * @Description
 */
public class Main {

    public static void main(String[] args) {

        AbstractPlamForm windows = new WindowsPlamForm();
        windows.setMediaPlayInter(new Mp4Play());
        windows.play();

        windows.setMediaPlayInter(new RmvbPlay());
        windows.play();



        AbstractPlamForm linux = new LinuxPlamForm();
        linux.setMediaPlayInter(new WmvPlay());
        linux.play();

    }
}
