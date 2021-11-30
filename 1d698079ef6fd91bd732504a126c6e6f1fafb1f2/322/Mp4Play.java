package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName Mp4Play
 * @date 2019/8/14  16:14.
 * @Description
 */
public class Mp4Play implements  MediaPlayInter {

    @Override
    public void mediaPlay() {
        System.out.println("Mp4 播放器");
    }
}
