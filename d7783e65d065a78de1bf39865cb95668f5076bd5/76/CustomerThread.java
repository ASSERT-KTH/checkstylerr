package Multithreading.GroceryCustomerSim;

public class CustomerThread extends Thread implements Customer {

    private boolean m_Served; //has been served
    private Queue m_Queue; //major queue this is a member of
    private long m_WaitTime; //time entered queue
    private long m_DoneTime; //time served

    public CustomerThread(Queue queue) {

        m_Queue = queue;
        m_Served = false;
        m_WaitTime = m_DoneTime = 0;
    }

    @Override
    public void run() {
        //record time
        m_WaitTime = m_DoneTime = System.currentTimeMillis();

        //enter queue and wait
        m_Queue.enterQueue(this);
        this.waitForService();
    }

    /**
     * After entering the Queue, the customer
     * should call this method, which should
     * wait until service has been provided, or
     * immediately return if service has already
     * been provided.
     * (This should be a synchronized method.)
     */
    public synchronized long waitForService() {
        try {
            this.wait();
        } catch( InterruptedException e) { return -1;}
        return 0;
    }

    /**
     * After a server removes the customer from
     * the queue, it will call this method which
     * will set the state of the customer to
     * serviced and notify the waiting customer
     * thread.
     * (This should be a synchronized method.)
     */
    public synchronized void provideService() {

        //record service time
        m_DoneTime = System.currentTimeMillis();
        m_Served = true;

        //notify waiting customer that he/she is currently being served
        this.notify();
    }

    /**
     * After a customer is finished waiting for
     * service, it will record the amount of
     * time it spent waiting, which can be
     * accessed with this method.
     */
    public long getWaitTime() {
        return m_DoneTime - m_WaitTime;
    }
}
