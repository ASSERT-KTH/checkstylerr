package com.hlj.moudle.design.D04_分开考虑.D10_Strategy模式;

/**
 * @author HealerJean
 * @ClassName MulSubStrategyImpl
 * @date 2019/8/14  17:10.
 * @Description
 */
public class MulSubStrategyImpl implements  StrategyInter{

    @Override
    public String calculate(Integer a, Integer b) {
        return "a * b = " + (a * b);
    }
}
