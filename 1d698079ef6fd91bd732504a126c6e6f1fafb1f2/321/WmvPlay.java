package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName WmvPlay
 * @date 2019/8/14  16:16.
 * @Description
 */
public class WmvPlay implements MediaPlayInter {

    @Override
    public void mediaPlay() {
        System.out.println("Wmv 播放器");
    }
}
