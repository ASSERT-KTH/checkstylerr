package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName RmvbPlay
 * @date 2019/8/14  16:16.
 * @Description
 */
public class RmvbPlay implements MediaPlayInter {

    @Override
    public void mediaPlay() {
        System.out.println("Rmvb 播放器");
    }
}
