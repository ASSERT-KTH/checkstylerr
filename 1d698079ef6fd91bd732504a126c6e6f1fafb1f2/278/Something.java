package com.hlj.moudle.design.D01_UML.D04_可见性_访问控制;

/**
 * @author HealerJean
 * @version 1.0v
 * @ClassName Something
 * @date 2019/5/24  12:36.
 * @Description
 */
public class Something {
    private int privateField ;
    int packageField ;
    protected int protectField ;
    public int publicField ;

    private void privateMethod() {}
    void defaultMethod () {}
    protected  void protectMethod(){}
    public void packageMethod() {}

}
