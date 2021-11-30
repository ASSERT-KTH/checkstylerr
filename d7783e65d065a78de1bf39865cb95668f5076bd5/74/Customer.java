package Multithreading.GroceryCustomerSim;

/**
 * This specifies the interface for the customer
 * that you'll need to implement as a thread object.
 */
public interface Customer {

    /**
     * After entering the Queue, the customer
     * should call this method, which should
     * wait until service has been provided, or
     * immediately return if service has already
     * been provided.
     * (This should be a synchronized method.)
     */
    public long waitForService();

    /**
     * After a server removes the customer from
     * the queue, it will call this method which
     * will set the state of the customer to
     * serviced and notify the waiting customer
     * thread.
     * (This should be a synchronized method.)
     */
    public void provideService();

    /**
     * After a customer is finished waiting for
     * service, it will record the amount of
     * time it spent waiting, which can be
     * accessed with this method.
     */
    public long getWaitTime();
}
