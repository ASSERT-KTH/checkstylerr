package com.hlj.moudle.design.D10用类来表现.D22Commond命令模式;

/**
 * @author HealerJean
 * @ClassName D22Main
 * @date 2019/8/21  18:36.
 * @Description
 */
public class D22Main {

    public static void main(String[] args) {
        //命令的执行者
        Receiver receiver = new Receiver();
        //命令--被哪个执行者执行
        Command cmd = new MyCommand(receiver);

        //调用命令
        Invoker invoker = new Invoker(cmd);
        invoker.action("杀人");
    }
}
