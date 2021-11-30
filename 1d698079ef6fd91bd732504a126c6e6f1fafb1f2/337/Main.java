package com.hlj.moudle.design.D04_分开考虑.D10_Strategy模式;

/**
 * @author HealerJean
 * @ClassName Main
 * @date 2019/8/14  17:10.
 * @Description
 */
public class Main {

    public static void main(String[] args) {

        int a = 10;
        int b = 5;

        StrategyInter addStrategy = new AddStrategyImpl();
        System.out.println(addStrategy.calculate(a, b));

        StrategyInter subStrategy = new SubStrategyImpl();
        System.out.println(subStrategy.calculate(a, b));

        StrategyInter mulSubStrategy = new MulSubStrategyImpl();
        System.out.println(mulSubStrategy.calculate(a, b));

    }
}
