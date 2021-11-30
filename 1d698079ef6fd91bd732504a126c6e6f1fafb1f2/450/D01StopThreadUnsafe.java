package com.hlj.moudle.thread.d05stop停止线程;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * 作者 ：HealerJean
 * 日期 ：2019/2/16  下午6:31.
 * 类描述： 这个类实际上并没有什么意义 ，stop已经被弃用了
 */
public class D01StopThreadUnsafe {

    public static User user = new User();

    @Data
    @Accessors(chain = true)
    @ToString
    public static class User {
        private int id;
        private String name;
    }


    public static class ChangeObjectThread extends Thread {
        public void run() {
            while (true) {
                synchronized (user) {
                    int v = (int) (System.currentTimeMillis() / 1000);
                    user.setId(v);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    user.setName(v + "");
                }
                Thread.yield();
            }
        }
    }

    public static class ReadObjectThread extends Thread {
        public void run() {
            while (true) {
                synchronized (user) {
                    if (user.getId() != Integer.parseInt(user.getName())) {
                        System.out.println(user.toString());
                    }
                }
                Thread.yield();
            }
        }
    }


    public static void main(String args[]) throws InterruptedException {

        new ReadObjectThread().start();

        while (true) {
            Thread thread = new ChangeObjectThread();
            thread.start();
            Thread.sleep(150);
            thread.stop();
        }


    }
}

