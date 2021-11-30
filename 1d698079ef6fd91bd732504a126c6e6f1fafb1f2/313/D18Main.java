package com.hlj.moudle.design.D08管理状态.D18Memento备忘录模式;

/**
 * @author HealerJean
 * @ClassName D18Main
 * @date 2019/8/20  18:28.
 * @Description
 */
public class D18Main {

    public static void main(String[] args) {

        //需要保存的数据
        Originator originator = new Originator();

        //备忘录管理，负责存储历史状态
        MementoManager manager = new MementoManager();
        originator.setState("2017-01-01");
        originator.setX(1.4f);
        originator.setY(5.4f);
        manager.add(originator.saveToMemento());

        originator.setState("2017-04-03");
        originator.setX(44.4f);
        originator.setY(52.4f);
        manager.add(originator.saveToMemento());

        originator.setState("2017-06-01");
        originator.setX(231.4f);
        originator.setY(555.4f);
        //记录状态
        manager.add(originator.saveToMemento());

        originator.setState("2017-06-22");
        originator.setX(132.4f);
        originator.setY(53.4f);
        //记录状态
        manager.add(originator.saveToMemento());

        System.out.println("当前状态：");
        System.out.println(originator.getState() + ": " + originator.getX() + ", " + originator.getY());


        System.out.println("状态历史：");
        for (Memento m : manager.getMementoList()) {
            System.out.println(m.getState() + ": " + m.getX() + ", " + m.getY());
        }


        //恢复到指定状态--2017-04-03
        originator.restoreFromMemento(manager.getByState("2017-04-03"));
        System.out.println("恢复后的状态：");
        System.out.println(originator.getState() + ": " + originator.getX() + ", " + originator.getY());
    }
}
