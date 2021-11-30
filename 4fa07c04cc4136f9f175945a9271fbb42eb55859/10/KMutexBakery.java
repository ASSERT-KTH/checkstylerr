package MutualExclusion;

public class KMutexBakery implements Lock {
    private int n_process;
    private int k_mutex;

    private volatile boolean[] choosing;
    private volatile int[] number;

    protected boolean smaller(int j, int i) {
        return number[j] < number[i] || (number[j] == number[i] && j < i);
    }

    public KMutexBakery(int n_process, int k_mutex) {
        this.n_process = n_process;
        this.k_mutex = k_mutex;

        choosing = new boolean[n_process];
        number = new int[n_process];
        for (int j = 0; j < n_process; j++) {
            choosing[j] = false;
            number[j] = 0;
        }
    }

    public void requestCS(int i) {
        choosing[i] = true;
        for (int j = 0; j < n_process; j++)
            if (number[j] > number[i])
                number[i] = number[j];
        number[i]++;
        choosing[i] = false;

        while (true) {
            int numSmaller = 0;
            for (int j = 0; j < n_process; j++) {
                while (choosing[j]);
                if (number[j] != 0 && smaller(j, i))
                    numSmaller++;
            }
            if (numSmaller < k_mutex)
                break;
        }
    }

    public void releaseCS(int i) {
        number[i] = 0;
    }

    public static void main(String[] args) {
        int n_process = 10;
        int k_mutex = 2;
        Lock lock = new KMutexBakery(n_process, k_mutex);

        TestMutex process[] = new TestMutex[n_process];

        for (int i = 0; i < n_process; i++) {
            process[i] = new TestMutex(i, lock);
            process[i].start();
        }
    }
}
