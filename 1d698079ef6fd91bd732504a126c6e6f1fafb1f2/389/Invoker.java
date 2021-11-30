package com.hlj.moudle.design.D10用类来表现.D22Commond命令模式;

/**
 * @author HealerJean
 * @ClassName Invoker
 * @date 2019/8/21  18:36.
 * @Description
 */
public class Invoker {
    private Command command;

    public Invoker(Command command) {
        this.command = command;
    }

    public void action(String word){
        command.exe(word);
    }
}
