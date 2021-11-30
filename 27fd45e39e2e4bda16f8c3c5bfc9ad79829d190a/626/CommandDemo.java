package com.sourcemaking.command.second_example;

import java.lang.reflect.*;

/**
 * Java reflection and the Command design pattern *
 * Motivation. "Sometimes it is necessary to issue requests to objects
 * without knowing anything about the operation being requested or the
 * receiver of the request." The Command design pattern suggests
 * encapsulating ("wrapping") in an object all (or some) of the following:
 * an object, a method name, and some arguments. Java does not support
 * "pointers to methods", but its reflection capability will do nicely.
 * The "command" is a black box to the "client". All the client does is
 * call "execute()" on the opaque object. 
 */

class SimpleCommand {
    private int state;

    public SimpleCommand(int state) {
        this.state = state;
    }

    public void add(Integer value) {
        state += value.intValue();
    }

    public void addTwoOperands(Integer firstValue, Integer secondValue) {
        state = state + firstValue.intValue() + secondValue.intValue();
    }

    public int getState() {
        return state;
    }
}

class ReflectCommand {
    // the "encapsulated" object
    private Object receiver;
    // the "pre-registered" request
    private Method action;
    // the "pre-registered" request
    private Object[] args;

    public ReflectCommand(Object obj, String methodName, Object[] arguments) {
        this.receiver = obj;
        this.args = arguments;
        Class cls = obj.getClass();
        Class[] argTypes = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].getClass();
        }
        try {
            action = cls.getMethod(methodName, argTypes);
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public Object execute() {
        try {
            action.invoke(receiver, args);
            return receiver.getClass().getMethod("getState").invoke(receiver);
        }
        catch(IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }
}

public class CommandDemo {
    public static void main(String[] args) {
        SimpleCommand[] simpleCommands = {new SimpleCommand(1), new SimpleCommand(2)};
        System.out.print("Normal call results: ");
        simpleCommands[0].add(3);
        System.out.print(simpleCommands[0].getState() + " ");
        simpleCommands[1].addTwoOperands(4, 5);
        System.out.print(simpleCommands[1].getState());

        ReflectCommand[] reflectCommands = {
                new ReflectCommand(simpleCommands[0], "add", new Integer[] {3}),
                new ReflectCommand(simpleCommands[1], "addTwoOperands", new Integer[] {4, 5})
        };

        System.out.print("\nReflection results:  ");
        for (ReflectCommand command : reflectCommands) {
            System.out.print(command.execute() + " ");
        }
        System.out.println();
    }
}
