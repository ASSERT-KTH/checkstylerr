package MutualExclusion;

public class Semaphore implements Lock {
    private CountingSemaphore semaphore;

    public Semaphore(int k_mutual) {
        semaphore = new CountingSemaphore(k_mutual);
    }

    public void requestCS(int i) {
        semaphore.P();
    }

    public void releaseCS(int i) {
        semaphore.V();
    }
}
