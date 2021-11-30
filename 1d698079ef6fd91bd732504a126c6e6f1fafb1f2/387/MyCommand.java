package com.hlj.moudle.design.D10用类来表现.D22Commond命令模式;

/**
 * @author HealerJean
 * @ClassName MyCommand
 * @date 2019/8/21  18:36.
 * @Description
 */
public class MyCommand implements Command {

    private Receiver receiver;

    public MyCommand(Receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void exe(String word) {
        receiver.action(word);
    }
}
