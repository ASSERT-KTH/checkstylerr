package com.hlj.moudle.design.D08管理状态.D18Memento备忘录模式;

import lombok.Data;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Originator
 * @Date 2019/8/20  18:27.
 * @Description 发起人角色，希望存储自己的历史状态
 */
@Data
public class Originator {

    private String state;
    private float x;
    private float y;

    /**
     * 保存到备忘录
     * @return
     */
    public Memento saveToMemento() {
        return new Memento(state, x, y);
    }

    /**
     * 从备忘录恢复
     * @param memento
     */
    public void restoreFromMemento(Memento memento) {
        this.state = memento.getState();
        this.x = memento.getX();
        this.y = memento.getY();
    }

}
