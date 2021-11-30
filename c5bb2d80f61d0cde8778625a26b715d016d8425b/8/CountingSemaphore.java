package MutualExclusion;

import utils.Util;

public class CountingSemaphore implements Lock {
    private int value;

    public CountingSemaphore(int iv) {
        value = iv;
    }

    public synchronized void P() {
        value--;
        if (value < 0)
            Util.wait(this);
    }

    public synchronized void V() {
        value++;
        notify();
    }

    public void requestCS(int id) {
        P();
    }

    public void releaseCS(int id) {
        V();
    }

    public static void main(String[] args) {
        int n_process = 5;
        int k_mutex = 3;
        Lock lock = new CountingSemaphore(k_mutex);

        TestMutex process[] = new TestMutex[n_process];

        for (int i = 0; i < n_process; i++) {
            process[i] = new TestMutex(i, lock);
            process[i].start();
        }
    }
}
