package Multithreading.GroceryCustomerSim;

public interface Queue {


    /**
     * This method is called by the simulation immediately
     * before a server thread is started, so the queue can
     * keep track of available servers.
     * Note: You can assume that all servers are added by
     * the simulation before any server or customer thread
     * is started.
     */
    public void addServer(Server server);

    /**
     * Called by customer threads immediately before they
     * wait for service.
     */
    public void enterQueue(Customer customer);

    /**
     * Called by servers to request another customer to serve.
     * If no customer is available for the specified server,
     * the calling thread should wait until one becomes available
     * or until the Queue is closed. If the queue has been closed,
     * this function returns null.
     */
    public Customer nextCustomer(Server server) throws InterruptedException;

    /**
     * Closes the queue. After being called, nextCustomer should
     * return null, and any servers still waiting for customers
     * should be notified.
     */
    public void close();

}
