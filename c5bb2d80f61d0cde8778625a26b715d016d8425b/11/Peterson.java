package MutualExclusion;

public class Peterson implements Lock {
    private volatile boolean wantCS[] = {false, false};
    private volatile int turn = 1;

    public void requestCS(int i) {
        int j = 1 - i;
        wantCS[i] = true;
        turn = j;
        while (wantCS[j] && (turn == j));
    }

    public void releaseCS(int i) {
        wantCS[i] = false;
    }

    public static void main(String[] args) {
        Lock lock = new Peterson();

        TestMutex processA = new TestMutex(0, lock);
        TestMutex processB = new TestMutex(1, lock);

        processA.start();
        processB.start();
    }
}
