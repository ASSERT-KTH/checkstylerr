package Multithreading.GroceryCustomerSim;

import java.util.*;

public class GroceryRandom extends GroceryQueue {

    public LinkedList<Customer> laneChoice() {
        //get the list of queues
        Collection<LinkedList<Customer>> choices = m_Queues.values();

        //pick a random number
        int num = (int)(Math.random() * choices.size());

        int i = 0;
        //iterate through and find the random one
        for( LinkedList<Customer> choice : choices) {
            if( i++ >= num) return choice;
        }

        //should never reach
        return choices.iterator().next();
    }
}

