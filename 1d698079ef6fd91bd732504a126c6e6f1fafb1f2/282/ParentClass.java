package com.hlj.moudle.design.D01_UML.D01_类与层次结构;


/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName ParentClass
 * @Date 2019/7/31  12:04.
 * @Description 类图
 */
public abstract class ParentClass {
    int field1;
    static char field2;

    abstract void methodA();

    double methodB() {
        return 0;
    }
}

class childClass extends ParentClass {
    void methodA() {
    }

    static void methodC() {
    }
}
