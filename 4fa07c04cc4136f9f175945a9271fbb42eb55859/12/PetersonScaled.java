package MutualExclusion;

public class PetersonScaled implements Lock {
    private int n_process;
    private Peterson[] PA;

    public PetersonScaled(int n_process) {
        this.n_process = n_process;
        PA = new Peterson[n_process];
        for (int j = 0; j < n_process; j++) {
            PA[j] = new Peterson();
        }
    }

    private void _requestCS(int i) {
        if (i == 1)
            return;

        PA[i >> 1].requestCS(i & 1);
        _requestCS(i >> 1);
    }

    private void _releaseCS(int i) {
        if (i == 1)
            return;

        _releaseCS(i >> 1);
        PA[i >> 1].releaseCS(i & 1);
    }

    public void requestCS(int i) {
        _requestCS(n_process + i);
    }

    public void releaseCS(int i) {
        _releaseCS(n_process + i);
    }

    public static void main(String[] args) {
        int n_process = 4;
        Lock lock = new PetersonScaled(n_process);

        TestMutex process[] = new TestMutex[n_process];

        for (int i = 0; i < n_process; i++) {
            process[i] = new TestMutex(i, lock);
            process[i].start();
        }
    }
}
