package com.hlj.moudle.design.D08管理状态.D18Memento备忘录模式;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author HealerJean
 * @ClassName MementoManager
 * @date 2019/8/20  18:29.
 * @Description 备忘录管理者
 */
public class MementoManager {

    private List<Memento> mementos;



    public MementoManager(){
        mementos = new ArrayList<>();
    }

    public void add(Memento memento){
        mementos.add(memento);
    }


    public List<Memento> getMementoList() {
        return mementos;
    }

    /**
     * 通过状态恢复
     */
    public Memento getByState(String status) {
        for (Memento memento :mementos){
            if(status.equals(memento.getState())){
                return memento;
            }
        }
        return null ;
    }
}
