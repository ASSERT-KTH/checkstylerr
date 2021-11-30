package com.hlj.moudle.design.D08管理状态.D18Memento备忘录模式;

import lombok.Data;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Memento
 * @Date 2019/8/20  18:26.
 * @Description 备忘录角色，存储发起人的装
 */
@Data
public class Memento {

    private String state;
    private float x;
    private float y;

    public Memento(String state, float x, float y) {
        this.state = state;
        this.x = x;
        this.y = y;
    }

}
