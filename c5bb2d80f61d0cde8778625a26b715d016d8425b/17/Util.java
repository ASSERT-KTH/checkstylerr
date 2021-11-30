package utils;

import java.util.concurrent.ThreadLocalRandom;

public class Util {
    public static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {}
    }
    public static void sleep(int min, int max) {
        int duration = ThreadLocalRandom.current().nextInt(min, max);
        sleep(duration);
    }
    public static void wait(Object obj) {
        try {
            obj.wait();
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }
}
