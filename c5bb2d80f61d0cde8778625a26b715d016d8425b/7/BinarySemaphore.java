package MutualExclusion;

import utils.Util;

public class BinarySemaphore implements Lock {
    private boolean value = true;

    public synchronized void P() {
        while (value == false)
            Util.wait(this);
        value = false;
    }

    public synchronized void V() {
        value = true;
        notify();
    }

    public void requestCS(int id) {
        P();
    }

    public void releaseCS(int id) {
        V();
    }

    public static void main(String[] args) {
        Lock lock = new BinarySemaphore();

        TestMutex processA = new TestMutex(0, lock);
        TestMutex processB = new TestMutex(1, lock);

        processA.start();
        processB.start();
    }
}
