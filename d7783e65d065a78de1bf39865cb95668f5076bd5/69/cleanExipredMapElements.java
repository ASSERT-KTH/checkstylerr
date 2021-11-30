package Multithreading.CleanExipredMapElements;

/**
 * Created by Chaklader on 1/15/17.
 */

/* How to Remove expired elements from HashMap and Add more elements
at the Same Time using Java Timer, TimerTask and futures()*/


/* Hashmap, ArrayList, Static Map, Vectors, etc are the most used Java collection framework elements.


1. Create Map Object
2. Keep adding element to Map every second which has expire time set to 5 seconds
3. Check for expired element like cache every second and delete from map if expired
4. After 5 seconds you will get always same size and always 5 elements as you are adding and deleting
expired elements every second

Some additional questions
-------------------------
1. What is Passive Expiring Map Example
2. Concurrent Map with timed out element
3. Java Cache Map Example
4. Java TimerTask Example
5. Evictor – a Java Concurrent Map Example



Point-1
=======

Create Timer element Timer
Schedules the specified task Reminder() for repeated fixed-delay execution which is 1 second
In scheduled task
add element into Map
check for expired element from Map and delete

Point-2
=======

During addElement() operation
We are associating current time for each element
During ClearExipredElementsFromMap() operation
We are checking for current time with element’s time
If time difference is more than 5 seconds then just delete element from Map

Point-3
=======

During add and remove operation print element on Eclipse console
1st added element will be removed 1st and so on
Please check Eclipse console output for result */


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;


/**
 * <p>
 * - Keep adding element to Map every second which has expire time set to 5 seconds
 * - Check for expired element every second and delete from map if expired
 * - After 5 seconds you will get always same size as you are adding and deleting expired elements every second
 */


public class cleanExipredMapElements {

    Timer timer;
    private static long EXPIRED_TIME_IN_SEC = 5l;
    private static Map<Double, ArrayList<Date>> map = new HashMap<Double, ArrayList<Date>>();

    public cleanExipredMapElements(int seconds) {
        timer = new Timer();
        timer.schedule(new reminder(), 0, seconds * 1000);
    }

    public void addElement() {
        addElementToMap(Math.random(), map);
    }

    class reminder extends TimerTask {

        public void run() {
            // We are checking for expired element from map every second
            clearExipredElementsFromMap(map);
            // We are adding element every second
            addElement();
        }
    }

    // Check for element's expired time. If element is > 5 seconds old then remove it
    private static void clearExipredElementsFromMap(Map<Double, ArrayList<Date>> map) {

        Date currentTime = new Date();
        Date actualExpiredTime = new Date();

        // if element time stamp and current time stamp difference is 5 second then delete element
        actualExpiredTime.setTime(currentTime.getTime() - EXPIRED_TIME_IN_SEC * 1000l);
        System.out.println("map size:" + map.size());

        Iterator<Entry<Double, ArrayList<Date>>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<Double, ArrayList<Date>> entry = iterator.next();
            ArrayList<Date> element = entry.getValue();

            while (element.size() > 0
                    && element.get(0).compareTo(actualExpiredTime) < 0) {
                log("----------- Element Deleted: " + entry.getKey());
                element.remove(0);
            }

            if (element.size() == 0) {
                iterator.remove();
            }
        }
    }

    // Adding new element to map with current timestamp
    private static void addElementToMap(Double digit, Map<Double, ArrayList<Date>> myMap) {
        ArrayList<Date> list = new ArrayList<Date>();
        myMap.put(digit, list);
        list.add(new Date());
        log("+++++++++++ Element added:" + digit + "\n");
    }

    private static void log(String string) {
        System.out.println(string);

    }

    public static void main(String args[]) {
        new cleanExipredMapElements(1);
        log("Start Adding element every second\n\n");
    }
}
