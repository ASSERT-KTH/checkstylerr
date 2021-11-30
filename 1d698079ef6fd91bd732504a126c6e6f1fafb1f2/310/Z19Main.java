package com.hlj.moudle.design.D08管理状态.D19Status状态模式;

/**
 * @author HealerJean
 * @ClassName Z19Main
 * @date 2019-08-20  21:07.
 * @Description
 */
public class Z19Main {

    public static void main(String[] args) {
        Context context = new Context();
        context.setLiftState(new ClosingState());

        context.open();
        System.out.println("--------");
        context.close();
        System.out.println("--------");
        context.run();
        System.out.println("--------");
        context.stop();

    }
}
