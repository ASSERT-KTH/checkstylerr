package MutualExclusion;

import utils.Util;

public class TestMutex extends Thread {
    private int id;
    private Lock lock;

    private static int numProcessInCS = 0;
    private static Object obj = new Object();

    public TestMutex(int id, Lock lock) {
        this.id = id;
        this.lock = lock;
    }

    private void CriticalSection() {
        synchronized (obj) {
            numProcessInCS++;
            System.out.println(id + " enters CS. " + numProcessInCS + " process in CS.");
        }
    }

    private void nonCriticalSection() {
        synchronized (obj) {
            numProcessInCS--;
            System.out.println(id + " exits CS.  " + numProcessInCS + " process in CS.");
        }
    }

    public void run() {
        while (true) {
            lock.requestCS(id);
            CriticalSection();
            Util.sleep(100, 1000);
            nonCriticalSection();
            lock.releaseCS(id);
            Util.sleep(100, 1000);
        }
    }
}