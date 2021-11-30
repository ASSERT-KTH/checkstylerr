package com.baeldung.concurrent.skiplist;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/*
ConcurrentSkipListMap class from the java.util.concurrent package. This construct allows 
us to create thread-safe logic in a lock-free way.  It’s ideal for problems when we want 
to make an immutable snapshot of the data while other threads are still inserting data 
into the map.

We will be solving a problem of sorting a stream of events and getting a snapshot of the 
events that arrived in the last 60 seconds using that construct. The ConcurrentSkipListMap 
will handle the sorting of those events underneath using the Comparator that was passed to 
it in the constructor. The most notable pros of the ConcurrentSkipListMap are the methods 
that can make an immutable snapshot of its data in a lock-free way. To get all events that 
arrived within the past minute, we can use the "tailMap" method and pass the time from 
which we want to get elements. 

It will return all events from the past minute. It will be an immutable snapshot and 
what is the most important is that other writing threads can add  new events to the 
ConcurrentSkipListMap without any need to do explicit locking.

We can now get all events that arrived later that one minute from now – by using the 
"headMap" method.

SKIPLIST
--------

Consider a sorted multi-level list. We start out with a regular singly-linked list 
that connects nodes in-order. Then, we add a level-2 list that skips every other 
node. And a level-3 list that skips every other node in the level-2 list. And so 
forth, until we have a list that jumps somewhere past the middle element. Checking 
whether a particular element is in the set only takes O(log N). The search algorithm 
is a lot like binary search.


The time complexity of basic operations on a skip list is as follows:

  ---------------------------------|
  Operation         Time Complexity|
  ---------------------------------|
  Insertion             O(log N)   |
  Removal               O(log N)   | 
  Check if contains     O(log N)   |
  Enumerate in order    O(N)       |
  ---------------------------------|



A sorted singly-linked list is not a terribly interesting data structure. The complexity of basic operations looks like this:

-------------------------------------
Operation             Time Complexity
-------------------------------------
Insertion               O(N)
Removal                 O(N)
Check if contains       O(N)
Enumerate in order      O(N)
-------------------------------------
*/
class EventWindowSort {


    private final ConcurrentSkipListMap<ZonedDateTime, String> events
      = new ConcurrentSkipListMap<>(Comparator.comparingLong(value -> value.toInstant().toEpochMilli()));

    void acceptEvent(Event event) {
        events.put(event.getEventTime(), event.getContent());
    }

    ConcurrentNavigableMap<ZonedDateTime, String> getEventsFromLastMinute() {
        return events.tailMap(ZonedDateTime
          .now()
          .minusMinutes(1));
    }

    ConcurrentNavigableMap<ZonedDateTime, String> getEventsOlderThatOneMinute() {
        return events.headMap(ZonedDateTime
          .now()
          .minusMinutes(1));
    }
}

