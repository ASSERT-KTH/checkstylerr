package Multithreading.GroceryCustomerSim;

public class Simulation {

    // Let's assume the number of the customer in the grocery shop is 1000
    public static final int CUSTOMERS = 1000;
    public static final int SERVERS = 5;
    public static final double MEAN_INTERARRIVAL_TIME = 8;
    public static final double MEAN_SERVICE_TIME = 32;

    public static int s_Customers = CUSTOMERS;

    public static void main(String[] args) {
        // TODO: Run the simulation with each of the three
        // queues, and print out the results.
        
        try {
            s_Customers = Integer.parseInt( args[0]);
        } catch( Exception e) {}

        System.out.println("Simulation start...");

        BankQueue bank = new BankQueue();
        try {
            System.out.printf("Bank Queue: %f milliseconds\n", simulate(bank));
        } catch( InterruptedException e) {
            System.out.println("ERROR: Bank Queue interrupted.");
        }

        GroceryShortest grocShort = new GroceryShortest();
        try {
            System.out.printf("Grocery Queue (shortest): %f milliseconds\n", simulate(grocShort));
        } catch( InterruptedException e) {
            System.out.println("ERROR: Grocery Queue (shortest) interrupted.");
        }

        GroceryRandom grocRand = new GroceryRandom();
        try {
            System.out.printf("Grocery Queue (random): %f milliseconds\n", simulate(grocRand));
        } catch( InterruptedException e) {
            System.out.println("ERROR: Grocery Queue (random) interrupted.");
        }
        
        System.out.println("Simulation complete.");
    }

    /**
     * Simulates the given queue, and returns the average wait time of a customer.
     */
    public static double simulate(Queue queue) throws InterruptedException {

        // For recording customer wait time
        double totalWaitTime = 0;

        // Create server threads, add to queue, and start
        ServerThread[] servers = new ServerThread[SERVERS];

        for (int i = 0; i < SERVERS; i++) {
            ServerThread server = new ServerThread(queue, MEAN_SERVICE_TIME);
            servers[i] = server;
            queue.addServer(server);
        }

        for( ServerThread server : servers) {
            server.start();
        }

        /******************** TODO ********************/
        // Create and start customer threads, using
        // exponentialSleep(MEAN_INTERARRIVAL_TIME);
        // between starting each customer.
        /**********************************************/
        //Create customer threads

        CustomerThread[] customers = new CustomerThread[s_Customers];
        for( int i = 0; i < s_Customers; i++) {
            CustomerThread customer = new CustomerThread( queue);
            customers[i] = customer;
        }
        for( CustomerThread customer : customers) {
            //wait for the customer to arrive
            exponentialSleep( MEAN_INTERARRIVAL_TIME);
            customer.start();
        }

        /******************** TODO ********************/
        // Join in each customer thread, and add its
        // wait time (using getWaitTime()) to the
        // totalWaitTime.
        /**********************************************/
        for( CustomerThread customer : customers) {
            customer.interrupt();
            customer.join();
            totalWaitTime += customer.getWaitTime();
        }

        // Close the queue
        queue.close();

        // Interrupt and join in server threads
        for (ServerThread server : servers) {
            server.interrupt();
            server.join();
        }

        return totalWaitTime / s_Customers;
    }

    /**
     * Sleeps for an exponentially distributed random amount of time,
     * with given average.
     */
    public static void exponentialSleep(double meanSleepTime) throws InterruptedException {
        long k = Math.round(-Math.log(Math.random()) * meanSleepTime);
        Thread.sleep(k);
    }
}
