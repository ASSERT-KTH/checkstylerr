package primitive.atomic;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic 测试
 */

public class AtomicTest {

  private static final int THREADS_COUNT = 20;
  private static final AtomicInteger race = new AtomicInteger();

  private static void increase() {
    race.incrementAndGet();
  }

  public static void main(String[] args) {
    Thread[] threads = new Thread[THREADS_COUNT];

    for (int i = 0; i < THREADS_COUNT; ++i) {
      threads[i] = new Thread(() -> {
        for (int j = 0; j < 10000; j++) {
          increase();
        }
      });
      threads[i].start();
    }

    while (Thread.activeCount() > 1) {
      Thread.yield();
    }

    System.out.println(race.get());
  }
}
