package com.hlj.moudle.design.D04_分开考虑.D09_Bridge模式.demo01;

/**
 * @author HealerJean
 * @ClassName AbstractPlamForm
 * @date 2019/8/14  16:19.
 * @Description
 */
public abstract class AbstractPlamForm {

    MediaPlayInter mediaPlayInter ;

    public void setMediaPlayInter(MediaPlayInter mediaPlayInter){
        this.mediaPlayInter = mediaPlayInter ;
    }


    public abstract void play();


}
