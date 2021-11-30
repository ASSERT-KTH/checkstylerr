package Multithreading.GroceryCustomerSim;

import java.util.*;
import java.util.Queue;


public class BankQueue implements Queue {
    
    private ArrayList<Server> m_Servers;
    private LinkedList<Customer> m_Customers;
    private boolean m_Closed;

    public BankQueue() {
        m_Servers = new ArrayList<Server>();
        m_Customers = new LinkedList<Customer>();
        m_Closed = false;
    }

    /**
     * This method is called by the simulation immediately
     * before a server thread is started, so the queue can
     * keep track of available servers.
     * Note: You can assume that all servers are added by
     * the simulation before any server or customer thread
     * is started.
     */
    public synchronized void addServer(Server server) {
        m_Servers.add(server);
    }

    /**
     * Called by customer threads immediately before they
     * wait for service.
     */
    public synchronized void enterQueue(Customer customer) {
        m_Customers.add(customer);
        this.notifyAll();
    }

    /**
     * Called by servers to request another customer to serve.
     * If no customer is available for the specified server,
     * the calling thread should wait until one becomes available
     * or until the Queue is closed. If the queue has been closed,
     * this function returns null.
     */
    public synchronized Customer nextCustomer(Server server) throws InterruptedException {

        Customer ret = m_Customers.poll();

        while( !m_Closed && ret == null) {

            this.wait();
            ret = m_Customers.poll();
        }
        return ret;
    }

    /**
     * Closes the queue. After being called, nextCustomer should
     * return null, and any servers still waiting for customers
     * should be notified.
     */
    public synchronized void close() {
        this.m_Closed = true;
        this.notifyAll();
    }
}
