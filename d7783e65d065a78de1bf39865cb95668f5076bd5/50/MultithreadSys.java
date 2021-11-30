package Multithreading;

import utils.AssortedMethods;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
* Created by Chaklader on 1/14/17.
*/


/*question 1: design a program to perform
multi-threading using Runnable interface*/
class CountThread implements Runnable {

    Thread mythread;

    CountThread() {
        mythread = new Thread(this, "my runnable thread");
        System.out.println("my thread created" + mythread);
        mythread.start();
    }

    public void run() {

        try {
            for (int i = 0; i <= 10; i++) {
                System.out.println("Printing the count " + i);
                Thread.sleep(2000);
            }
        } catch (InterruptedException e) {
            System.out.println("my thread interrupted");
        }
        System.out.println("mythread run is over");
    }
}
/*END of solution 1: design a program to perform
multi-threading using Runnable interface*/


/*question 2: design a program to perform
multi-threading by extending the Thread
class*/
class CountThread1 extends Thread {

    CountThread1() {
        super("my extending thread");
        System.out.println("my thread created" + this);
        start();
    }

    @Override
    public void run() {

        try {
            for (int i = 0; i < 10; i++) {
                System.out.println("Printing the count " + i);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            System.out.println("my thread interrupted");
        }
        System.out.println("My thread run is over");
    }
}
/*END of solution 2: design a program to perform
multi-threading by extending the Thread class*/


/*question 3: design a program to perform
multi-threading using Runnable interface*/
class RunnableDemo implements Runnable {

    private Thread t;
    private String threadName;

    RunnableDemo(String name) {

        threadName = name;
        System.out.println("Creating " + threadName);
    }

    public void run() {

        System.out.println("Running " + threadName);
        try {
            for (int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {

        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
/*END of solution 3: design a program to perform
multi-threading using Runnable interface*/


/*question 4: design a program to perform
multi-threading by extending the Thread class*/
class ThreadDemo extends Thread {

    private Thread t;
    private String threadName;

    ThreadDemo(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);
    }

    @Override
    public void run() {

        System.out.println("Running " + threadName);
        try {
            for (int i = 4; i > 0; i--) {
                System.out.println("Thread: " + threadName + ", " + i);
                // Let the thread sleep for a while.
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }

        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}
/*END of solution 4: design a program to perform
multi-threading by extending the Thread class*/


/*question 5: design a program to implement
number guessing program*/
class DisplayMessage implements Runnable {

    private String message;

    public DisplayMessage(String message) {
        this.message = message;
    }

    public void run() {
        while (true) {
            System.out.println(message);
        }
    }
}

class GuessANumber extends Thread {

    private int number;

    public GuessANumber(int number) {
        this.number = number;
    }

    @Override
    public void run() {

        int counter = 0;
        int guess = 0;

        do {
            guess = (int) (Math.random() * 100 + 1);
            System.out.println(this.getName()
                    + " guesses " + guess);
            counter++;
        }

        while (guess != number);
        System.out.println("** Correct! " + this.getName()
                + " in " + counter + " guesses.**");
    }
}
/*END of solution 5: design a program to
implement number guessing program*/


/*question 6: write a program to impelment
multi-threading without syncronization*/
class MyThreadDemo extends Thread {

    private Thread t;
    private String threadName;
    PrintDemo PD;

    MyThreadDemo(String name, PrintDemo pd) {
        this.threadName = name;
        this.PD = pd;
    }

    @Override
    public void run() {
        PD.printCount();
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {

        System.out.println("Starting " + threadName);

        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}

class PrintDemo {

    public void printCount() {

        try {
            for (int i = 5; i > 0; i--) {
                System.out.println("Counter - " + i);
            }
        } catch (Exception e) {
            System.out.println("Thread  interrupted.");
        }
    }
}
/*END of solution 6: write a program
to impelment multi-threading without
syncronization*/


/*question 7: write a program to impelment
multi-threading with syncronization*/
class ThreadDemo1 extends Thread {

    private Thread t;
    private String threadName;
    PrintDemo1 PD;

    ThreadDemo1(String name, PrintDemo1 pd) {
        this.threadName = name;
        this.PD = pd;
    }

    @Override
    public void run() {
        synchronized (PD) {
            PD.printCount();
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }
}

class PrintDemo1 {

    public void printCount() {

        try {
            for (int i = 5; i > 0; i--) {
                System.out.println("Counter   ---   " + i);
            }
        } catch (Exception e) {
            System.out.println("Thread  interrupted.");
        }
    }
}
/*END of silution 7: write a program
to impelment multi-threading with
syncronization*/


/*question 8: write a program to
impelment inter-thread communication*/
class Chat {

    boolean flag = false;

    public synchronized void Question(String msg) {

        if (flag) {
            
            try {
                wait();
            } 

            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(msg);
        flag = true;
        notify();
    }

    public synchronized void Answer(String msg) {

        if (!flag) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println(msg);
        flag = false;
        notify();
    }
}

class T1 implements Runnable {

    Chat m;
    String[] s1 = {"Hi", "How are you ?", "I am also doing fine!"};

    public T1(Chat m1) {
        this.m = m1;
        new Thread(this, "Question").start();
    }

    public void run() {
        for (int i = 0; i < s1.length; i++) {
            m.Question(s1[i]);
        }
    }
}

class T2 implements Runnable {

    Chat m;
    String[] s2 = {"Hi", "I am good, what about you?", "Great!"};

    public T2(Chat m2) {
        this.m = m2;
        new Thread(this, "Answer").start();
    }

    public void run() {
        for (int i = 0; i < s2.length; i++) {
            m.Answer(s2[i]);
        }
    }
}
/*ENd of solution 8: write a program to
impelment inter-thread communication*/


/*question 11: write a program to
impelment thread control*/
class RunnableDemoOne implements Runnable {

    public Thread t;
    private String threadName;
    boolean suspended = false;

    RunnableDemoOne(String name) {
        threadName = name;
        System.out.println("Creating " + threadName);
    }

    public void run() {

        System.out.println("Running " + threadName);

        try {

            for (int i = 10; i > 0; i--) {

                System.out.println("Thread: " + threadName + ", " + i);
                Thread.sleep(300);

                synchronized (this) {

                    while (suspended) {
                        wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {

        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

    void suspend() {
        suspended = true;
    }

    synchronized void resume() {
        suspended = false;
        notify();
    }
}
/*END of solution 11: write a program
to impelment thread control*/


/*question 12:  write a program
to impelment daemon thread*/
class DaemonThreadExample1 extends Thread {

    @Override
    public void run() {
        if (Thread.currentThread().isDaemon()) {
            System.out.println("Daemon thread executing");
        } else {
            System.out.println("user(normal) thread executing");
        }
    }
}
/*END of solution 12:  write a program
to impelment daemon thread*/


/*question 13: write a program
to impelment daemon thread exception*/
class DaemonThreadEx2 extends Thread {

    @Override
    public void run() {
        System.out.println("Thread is running");
    }
}
/*END of solution 13: write a program
to impelment daemon thread exception*/


/*question 14: write a program to impelment
sequential thread execuation without using
join() method*/

// in this case, using the join() method
// start and ending is not in the same order
class MyClass2 implements Runnable {

    public void run() {

        Thread t = Thread.currentThread();
        System.out.println("Thread started: " + t.getName());
        
        try {
            Thread.sleep(4000);
        } 

        catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        System.out.println("Thread ended: " + t.getName());
    }
}
/*END of solution 14: write a program to
impelment sequential thread execuation
without using join() method*/


/*question 15: write a program to impelment
sequential thread execuation using join() method*/
class MyClass implements Runnable {

    public void run() {

        Thread t = Thread.currentThread();
        System.out.println("Thread started: " + t.getName());

        try {
            Thread.sleep(4000);
        } 

        catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        System.out.println("Thread ended: " + t.getName());
    }
}
/*ENd of solution 15: write a program to
impelment sequential thread execuation
using join() method*/


/*question 16: write a program to impelment
calling run method directly*/
class RunMethodExample implements Runnable {

    // if we just call run method directly,
    // it will behave like normal method and no
    // multi-threading will happen
    public void run() {

        for (int i = 1; i <= 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            System.out.println(i);
        }
    }
}
/*END of solution 16: write a program to
impelment calling run method*/


/*question 17: write a program to impelment
calling run method from the start()*/
class RunMethodExample2 {

    public void run() {

        for (int i = 1; i <= 3; i++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            System.out.println(i);
        }
    }
}
/*END of solution 17: write a program
to impelment calling run method from
the start()*/


/*question-A*/
class RunnableThreadExample implements Runnable {

    public int count = 0;

    public void run() {
        System.out.println("RunnableThread starting.");
        try {
            while (count < 5) {
                Thread.sleep(500);
                System.out.println("RunnableThread count: " + count);
                count++;
            }
        } catch (InterruptedException exc) {
            System.out.println("RunnableThread interrupted.");
        }
        System.out.println("RunnableThread terminating.");
    }
}
/*END of solution-A*/


/*question-B*/
class ThreadExample extends Thread {

    int count = 0;

    @Override
    public void run() {
        System.out.println("Thread starting.");
        try {
            while (count < 5) {
                Thread.sleep(500);
                System.out.println("In Thread, count is " + count);
                count++;
            }
        } catch (InterruptedException exc) {
            System.out.println("Thread interrupted.");
        }
        System.out.println("Thread terminating.");
    }
}
/*END of solution-B*/


/*question-C*/
class LockedATM {

    private Lock lock;
    private int balance = 100;

    public LockedATM() {
        lock = new ReentrantLock();
    }

    public int withdraw(int value) {
        lock.lock();
        int temp = balance;
        try {
            Thread.sleep(100);
            temp = temp - value;
            Thread.sleep(100);
            balance = temp;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        return temp;
    }

    public int deposit(int value) {
        lock.lock();
        int temp = balance;
        try {
            Thread.sleep(100);
            temp = temp + value;
            Thread.sleep(100);
            balance = temp;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock.unlock();
        return temp;
    }

    public int getBalance() {
        return balance;
    }
}

class NoLockATM {

    private int balance = 100;

    public NoLockATM() {
        System.out.println("No Lock ATM Object");
    }

    public int withdraw(int value) {

        int temp = balance;
        try {
            Thread.sleep(300);
            temp = temp - value;
            Thread.sleep(300);
            balance = temp;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public int deposit(int value) {

        int temp = balance;
        try {
            Thread.sleep(300);
            temp = temp + value;
            Thread.sleep(300);
            balance = temp;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return temp;
    }

    public int getBalance() {
        return balance;
    }
}


class MyClass1 extends Thread {
    private NoLockATM noLockATM;
    private LockedATM lockedATM;
    public int delta = 0;

    private Lock completionLock;

    public MyClass1(NoLockATM atm1, LockedATM atm2) {
        noLockATM = atm1;
        lockedATM = atm2;
        completionLock = new ReentrantLock();
    }

    @Override
    public void run() {

        completionLock.lock();
        int[] operations = AssortedMethods.randomArray(20, -50, 50);

        for (int op : operations) {
            delta += op;
            if (op < 0) {
                int val = op * -1;
                noLockATM.withdraw(val);
                lockedATM.withdraw(val);
            } else {
                noLockATM.deposit(op);
                lockedATM.deposit(op);
            }
        }
        completionLock.unlock();
    }

    public void waitUntilDone() {
        completionLock.lock();
        completionLock.unlock();
    }
}
/*END of solution-C*/


/*question-D*/
class MyObject {

    public static synchronized void foo(String name) {
        try {
            System.out.println("Thread " + name + ".foo(): starting");
            Thread.sleep(3000);
            System.out.println("Thread " + name + ".foo(): ending");
        } catch (InterruptedException exc) {
            System.out.println("Thread " + name + ": interrupted.");
        }
    }

    public static synchronized void bar(String name) {
        try {
            System.out.println("Thread " + name + ".bar(): starting");
            Thread.sleep(3000);
            System.out.println("Thread " + name + ".bar(): ending");
        } catch (InterruptedException exc) {
            System.out.println("Thread " + name + ": interrupted.");
        }
    }
}

class MyClass3 extends Thread {

    private String name;
    private MyObject myObj;

    public MyClass3(MyObject obj, String n) {
        name = n;
        myObj = obj;
    }

    public void run() {
        if (name.equals("1")) {
            MyObject.foo(name);
        } else if (name.equals("2")) {
            MyObject.bar(name);
        }
    }
}
/*END of solution-D*/


/*question-E*/
class MyObject2 {

    public void foo(String name) {

        synchronized (this) {
            try {
                System.out.println("Thread " + name + ".foo(): starting");
                Thread.sleep(3000);
                System.out.println("Thread " + name + ".foo(): ending");
            } catch (InterruptedException exc) {
                System.out.println("Thread " + name + ": interrupted.");
            }
        }
    }
}


class MyClass4 extends Thread {

    private String name;
    private MyObject2 myObj;

    public MyClass4(MyObject2 obj, String n) {
        name = n;
        myObj = obj;
    }

    public void run() {
        myObj.foo(name);
    }
}
/*END of solution-E*/


/*question-F*/
class MyObject3 {

    public void foo(String name) {
        try {
            System.out.println("Thread " + name + ".foo(): starting");
            Thread.sleep(3000);
            System.out.println("Thread " + name + ".foo(): ending");
        } catch (InterruptedException exc) {
            System.out.println("Thread " + name + ": interrupted.");
        }
    }
}


class MyClass5 extends Thread {

    private String name;
    private MyObject3 myObj;

    public MyClass5(MyObject3 obj, String n) {
        name = n;
        myObj = obj;
    }

    public void run() {

        try {
            myObj.wait(1000);
            myObj.foo(name);
            myObj.notify();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
/*END of solution-F*/


/*question-G*/
class Philosopher extends Thread {

    private final int maxPause = 100;
    private int bites = 10;

    private Chopstick left;
    private Chopstick right;
    private int index;

    public Philosopher(int i, Chopstick left, Chopstick right) {
        index = i;
        this.left = left;
        this.right = right;
    }

    public void eat() {
        System.out.println("Philosopher " + index + ": start eating");
        if (pickUp()) {
            chew();
            putDown();
            System.out.println("Philosopher " + index + ": done eating");
        } else {
            System.out.println("Philosopher " + index + ": gave up on eating");
        }
    }

    public boolean pickUp() {

        pause();
        if (!left.pickUp()) {
            return false;
        }

        pause();
        if (!right.pickUp()) {
            left.putDown();
            return false;
        }

        pause();
        return true;
    }

    public void chew() {
        System.out.println("Philosopher " + index + ": eating");
        pause();
    }

    public void pause() {
        try {
            int pause = AssortedMethods.randomIntInRange(0, maxPause);
            Thread.sleep(pause);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putDown() {
        left.putDown();
        right.putDown();
    }

    public void run() {
        for (int i = 0; i < bites; i++) {
            eat();
        }
    }
}

class Chopstick {

    private Lock lock;

    public Chopstick() {
        lock = new ReentrantLock();
    }

    public boolean pickUp() {
        return lock.tryLock();
    }

    public void putDown() {
        lock.unlock();
    }
}


class Question {

    public static int size = 3;

    public static int leftOf(int i) {
        return i;
    }

    public static int rightOf(int i) {
        return (i + 1) % size;
    }

    public static void testMethod() {

        Chopstick[] chopsticks = new Chopstick[size + 1];
        for (int i = 0; i < size + 1; i++) {
            chopsticks[i] = new Chopstick();
        }

        Philosopher[] philosophers = new Philosopher[size];
        for (int i = 0; i < size; i++) {
            Chopstick left = chopsticks[leftOf(i)];
            Chopstick right = chopsticks[rightOf(i)];
            philosophers[i] = new Philosopher(i, left, right);
        }

        for (int i = 0; i < size; i++) {
            philosophers[i].start();
        }
    }
}
/*END of solution-G*/


/*question-H*/
class LockNode {

    public enum VisitState {
        FRESH, VISITING, VISITED
    }

    private ArrayList<LockNode> children;
    private int lockId;
    private Lock lock;
    private int maxLocks;

    public LockNode(int id, int max) {
        lockId = id;
        children = new ArrayList<LockNode>();
        maxLocks = max;
    }

    /* Join "this" to "node", checking to make sure that it doesn't create a cycle */
    public void joinTo(LockNode node) {
        children.add(node);
    }

    public void remove(LockNode node) {
        children.remove(node);
    }

    /* Check for a cycle by doing a depth-first-search. */
    public boolean hasCycle(Hashtable<Integer, Boolean> touchedNodes) {
        VisitState[] visited = new VisitState[maxLocks];
        for (int i = 0; i < maxLocks; i++) {
            visited[i] = VisitState.FRESH;
        }
        return hasCycle(visited, touchedNodes);
    }

    private boolean hasCycle(VisitState[] visited, Hashtable<Integer, Boolean> touchedNodes) {
        if (touchedNodes.containsKey(lockId)) {
            touchedNodes.put(lockId, true);
        }

        if (visited[lockId] == VisitState.VISITING) {
            return true;
        } else if (visited[lockId] == VisitState.FRESH) {
            visited[lockId] = VisitState.VISITING;
            for (LockNode n : children) {
                if (n.hasCycle(visited, touchedNodes)) {
                    return true;
                }
            }
            visited[lockId] = VisitState.VISITED;
        }
        return false;
    }

    public Lock getLock() {
        if (lock == null) {
            lock = new ReentrantLock();
        }
        return lock;
    }

    public int getId() {
        return lockId;
    }
}

class LockFactory {
    private static LockFactory instance;

    private int numberOfLocks = 5; /* default */
    private LockNode[] locks;

    /* Maps from a process or owner to the order that the owner claimed it would call the locks in */
    private Hashtable<Integer, LinkedList<LockNode>> lockOrder;

    private LockFactory(int count) {
        numberOfLocks = count;
        locks = new LockNode[numberOfLocks];
        lockOrder = new Hashtable<Integer, LinkedList<LockNode>>();
        for (int i = 0; i < numberOfLocks; i++) {
            locks[i] = new LockNode(i, count);
        }
    }

    public static LockFactory getInstance() {
        return instance;
    }

    public static LockFactory initialize(int count) {
        if (instance == null) {
            instance = new LockFactory(count);
        }
        return instance;
    }

    public boolean hasCycle(Hashtable<Integer, Boolean> touchedNodes, int[] resourcesInOrder) {
        /* check for a cycle */
        for (int resource : resourcesInOrder) {
            if (touchedNodes.get(resource) == false) {
                LockNode n = locks[resource];
                if (n.hasCycle(touchedNodes)) {
                    return true;
                }
            }
        }
        return false;
    }

    /* To prevent deadlocks, force the processes to declare upfront what order they will
     * need the locks in. Verify that this order does not create a deadlock (a cycle in a directed graph)
     */
    public boolean declare(int ownerId, int[] resourcesInOrder) {
        Hashtable<Integer, Boolean> touchedNodes = new Hashtable<Integer, Boolean>();

		/* add nodes to graph */
        int index = 1;
        touchedNodes.put(resourcesInOrder[0], false);

        for (index = 1; index < resourcesInOrder.length; index++) {
            LockNode prev = locks[resourcesInOrder[index - 1]];
            LockNode curr = locks[resourcesInOrder[index]];
            prev.joinTo(curr);
            touchedNodes.put(resourcesInOrder[index], false);
        }

		/* if we created a cycle, destroy this resource list and return false */
        if (hasCycle(touchedNodes, resourcesInOrder)) {
            for (int j = 1; j < resourcesInOrder.length; j++) {
                LockNode p = locks[resourcesInOrder[j - 1]];
                LockNode c = locks[resourcesInOrder[j]];
                p.remove(c);
            }
            return false;
        }

		/* No cycles detected. Save the order that was declared, so that we can verify that the
         * process is really calling the locks in the order it said it would. */
        LinkedList<LockNode> list = new LinkedList<LockNode>();
        for (int i = 0; i < resourcesInOrder.length; i++) {
            LockNode resource = locks[resourcesInOrder[i]];
            list.add(resource);
        }

        lockOrder.put(ownerId, list);
        return true;
    }

    /* Get the lock, verifying first that the process is really calling the locks in the order
     * it said it would. */
    public Lock getLock(int ownerId, int resourceID) {

        LinkedList<LockNode> list = lockOrder.get(ownerId);
        if (list == null) {
            return null;
        }

        LockNode head = list.getFirst();
        if (head.getId() == resourceID) {
            list.removeFirst();
            return head.getLock();
        }
        return null;
    }
}
/*ENd of solution-H*/


/*question-I*/
class Foo {

    public int pauseTime = 1000;
    public Semaphore sem1;
    public Semaphore sem2;

    public Foo() {
        try {
            sem1 = new Semaphore(1);
            sem2 = new Semaphore(1);

            sem1.acquire();
            sem2.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void first() {
        try {
            System.out.println("Started Executing 1");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 1");
            sem1.release();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void second() {
        try {
            sem1.acquire();
            sem1.release();
            System.out.println("Started Executing 2");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 2");
            sem2.release();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void third() {
        try {
            sem2.acquire();
            sem2.release();
            System.out.println("Started Executing 3");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 3");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class FooBad {

    public int pauseTime = 1000;
    public ReentrantLock lock1;
    public ReentrantLock lock2;

    public FooBad() {
        try {

            lock1 = new ReentrantLock();
            lock2 = new ReentrantLock();
            lock1.lock();
            lock2.lock();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void first() {
        try {
            System.out.println("Started Executing 1");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 1");
            lock1.unlock();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void second() {
        try {
            lock1.lock();
            lock1.unlock();
            System.out.println("Started Executing 2");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 2");
            lock2.unlock();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void third() {
        try {
            lock2.lock();
            lock2.unlock();
            System.out.println("Started Executing 3");
            Thread.sleep(pauseTime);
            System.out.println("Finished Executing 3");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

class MyThread00 extends Thread {

    private String method;
    private FooBad foo;

    public MyThread00(FooBad foo, String method) {
        this.method = method;
        this.foo = foo;
    }

    public void run() {
        if (method == "first") {
            foo.first();
        } else if (method == "second") {
            foo.second();
        } else if (method == "third") {
            foo.third();
        }
    }
}
/*END of solution-I*/


/*question-J*/
class Foo1 {

    private String name;

    public Foo1(String nm) {
        name = nm;
    }

    public String getName() {
        return name;
    }

    public void pause() {
        try {
            Thread.sleep(1000 * 3);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void methodA(String threadName) {
        System.out.println("thread " + threadName + " starting: " + name + ".methodA()");
        pause();
        System.out.println("thread " + threadName + " ending: " + name + ".methodA()");
    }

    public void methodB(String threadName) {
        System.out.println("thread " + threadName + " starting: " + name + ".methodB()");
        pause();
        System.out.println("thread " + threadName + " ending: " + name + ".methodB()");
    }
}

class MyThread01 extends Thread {

    private Foo1 foo;
    public String name;
    public String firstMethod;

    public MyThread01(Foo1 f, String nm, String fM) {
        foo = f;
        name = nm;
        firstMethod = fM;
    }

    @Override
    public void run() {
        if (firstMethod.equals("A")) {
            foo.methodA(name);
        } else {
            foo.methodB(name);
        }
    }
}
/*ENd of solution-J*/


/*the main class for the multi-threading*/
public class MultithreadSys {

    /*question 9: write a program to
    impelment thread dead lock situation*/
    public static Object Lock1 = new Object();
    public static Object Lock2 = new Object();

    private static class ThreadDemo1 extends Thread {

        @Override
        public void run() {

            synchronized (Lock1) {

                System.out.println("Thread 1: Holding lock 1...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
                System.out.println("Thread 1: Waiting for lock 2...");
                synchronized (Lock2) {
                    System.out.println("Thread 1: Holding lock 1 & 2...");
                }
            }
        }
    }

    private static class ThreadDemo2 extends Thread {

        @Override
        public void run() {
            synchronized (Lock2) {
                System.out.println("Thread 2: Holding lock 2...");

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 2: Waiting for lock 1...");
                synchronized (Lock1) {
                    System.out.println("Thread 2: Holding lock 1 & 2...");
                }
            }
        }
    }
    /*END of solution 9: write a program to
    impelment thread dead lock situation*/


    /*question 10: write a program to
    impelment the solution of then
    thread dead lock situation*/
    public static Object LockOne = new Object();
    public static Object LockTwo = new Object();

    private static class ThreadDemoOne extends Thread {

        @Override
        public void run() {

            synchronized (Lock1) {

                System.out.println("Thread 1: Holding lock 1...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("Thread 1: Waiting for lock 2...");

                synchronized (LockTwo) {
                    System.out.println("Thread 1: Holding lock 1 & 2...");
                }

            }
        }
    }

    private static class ThreadDemoTwo extends Thread {

        @Override
        public void run() {

            synchronized (LockOne) {
                System.out.println("Thread 2: Holding lock 1...");
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }

                System.out.println("Thread 2: Waiting for lock 2...");
                synchronized (LockTwo) {

                    System.out.println("Thread 2: Holding lock 1 & 2...");
                }
            }
        }
    }
    /*END of solution 10: write a program to
    impelment the solution of then
    thread dead lock situation*/


    /*testAllMethods all the examples here*/
    public static void testAllMethods() {

        /*question 1: design a program to perform
        multi-threading using Runnable interface*/

        /*
        Count cnt = new Count();

        try{
            while(cnt.mythread.isAlive()){
                System.out.println("Main thread will be alive till the child thread is live");
                Thread.sleep(1500);
            }
        }

        catch(InterruptedException e){
            System.out.println("Main thread interrupted");
        }

        System.out.println("Main thread run is over" );
        */
        /*END of solution 1: design a program to perform
        multi-threading using Runnable interface*/




        /*question 5: design a program to implement
        number guessing program*/

        /*
        Runnable hello = new DisplayMessage("Hello");
        Thread thread1 = new Thread(hello);
        thread1.setDaemon(true);
        thread1.setName("hello");
        System.out.println("Starting hello thread...");
        thread1.start();

        Runnable bye = new DisplayMessage("Goodbye");
        Thread thread2 = new Thread(bye);
        thread2.setPriority(Thread.MIN_PRIORITY);
        thread2.setDaemon(true);
        System.out.println("Starting goodbye thread...");
        thread2.start();

        System.out.println("Starting thread3...");
        Thread thread3 = new GuessANumber(27);
        thread3.start();

        try{

          thread3.join();
        }

        catch(InterruptedException e){
            System.out.println("Thread interrupted.");
        }

        System.out.println("Starting thread4...");
        Thread thread4 = new GuessANumber(75);

        thread4.start();
        System.out.println("main() is ending...");*/
        /*END of solution 5: design a program to implement
        number guessing program*/




        /*Question 6: write a program to impelment
        multi-threading without syncronization*/

        /*
        PrintDemo PD = new PrintDemo();

        MyThreadDemo T1 = new MyThreadDemo( "Thread - 1 ", PD );
        MyThreadDemo T2 = new MyThreadDemo( "Thread - 2 ", PD );

        T1.start();
        T2.start();

        // wait for threads to end
        try {

          T1.join();
          T2.join();
        }

        catch( Exception e) {

            System.out.println("Interrupted");
        }
        */
        /*END of solution 6: write a program
        to impelment multi-threading without
        syncronization*/




        /*Question 7: write a program
        to impelment multi-threading with
        syncronization*/

        /*
        PrintDemo1 PD = new PrintDemo1();

        ThreadDemo1 T1 = new ThreadDemo1( "Thread - 1 ", PD );
        ThreadDemo1 T2 = new ThreadDemo1( "Thread - 2 ", PD );

        T1.start();
        T2.start();

        // wait for threads to end
        try {

          T1.join();
          T2.join();
        }

        catch( Exception e) {
          System.out.println("Interrupted");
        }

        */
        /*END of silution 7: write a program
        to impelment multi-threading with
        syncronization*/




        /*question 8: write a program to
        impelment inter-thread communication*/

        /*
        Chat m = new Chat();
        new T1(m);
        new T2(m);*/

        /*END of silution 8: write a program to
        impelment inter-thread communication*/


        // this program will enter a dead situation
        /*question 9: write a program to
        impelment thread dead lock situation*/

        /*
        ThreadDemo1 T1 = new ThreadDemo1();
        ThreadDemo2 T2 = new ThreadDemo2();
        T1.start();
        T2.start();
        */

        /*END of silution 9: write a program to
        impelment thread dead lock situation*/




        /*question 10: write a program to
        impelment the solution of then
        thread dead lock situation*/

        /*
        ThreadDemoOne T1 = new ThreadDemoOne();
        ThreadDemoTwo T2 = new ThreadDemoTwo();
        T1.start();
        T2.start();
        */

        /*END of silution 10: write a program to
        impelment the solution of then
        thread dead lock situation*/




        /*Question 11: write a program
        to impelment thread control*/
        /*
        RunnableDemoOne R1 = new RunnableDemoOne( "Thread-1");
        R1.start();

        RunnableDemoOne R2 = new RunnableDemoOne( "Thread-2");
        R2.start();

        try {

            Thread.sleep(1000);
            R1.suspend();
            System.out.println("Suspending First Thread");
            Thread.sleep(1000);
            R1.resume();
            System.out.println("Resuming First Thread");
            R2.suspend();
            System.out.println("Suspending thread Two");
            Thread.sleep(1000);
            R2.resume();
            System.out.println("Resuming thread Two");
        }

        catch (InterruptedException e) {
            System.out.println("Main thread Interrupted");
        }

        try {
            System.out.println("Waiting for threads to finish.");
            R1.t.join();
            R2.t.join();
        }

        catch (InterruptedException e) {
            System.out.println("Main thread Interrupted");
        }

        System.out.println("Main thread exiting.");
        */
        /*END of solution 11: write a program
        to impelment thread control*/




        /*Question 12:  write a program
        to impelment daemon thread*/

        /*
        DaemonThreadExample1 t1=new DaemonThreadExample1();
        DaemonThreadExample1 t2=new DaemonThreadExample1();

        //Making user thread t1 to Daemon
        t1.setDaemon(true);

        //starting both the threads
        t1.start();
        t2.start();
        */
        /*END of solution 12:  write a program
        to impelment daemon thread*/




        /*Question 13: write a program
        to impelment daemon thread exception*/

        /*
        DaemonThreadEx2 t1=new DaemonThreadEx2();
        t1.start();
        // It will throw IllegalThreadStateException
        // because, the daemon state is bening set after the thread start
        t1.setDaemon(true);
        */
        /*END of solution 13: write a program
        to impelment daemon thread exception*/




        /*Question 14: write a program to
        impelment sequential thread execuation
        without using join() method*/

        /*
        Thread th1 = new Thread(new MyClass2(), "th1");
        Thread th2 = new Thread(new MyClass2(), "th2");
        Thread th3 = new Thread(new MyClass2(), "th3");

        th1.start();
        th2.start();
        th3.start();
        */
        /*END of solution 14: write a program to
        impelment sequential thread execuation
        without using join() method*/




        /*Question 15: write a program to
        impelment sequential thread execuation
        using join() method*/

        /*
        Thread th1 = new Thread(new MyClass(), "th1");
        Thread th2 = new Thread(new MyClass(), "th2");
        Thread th3 = new Thread(new MyClass(), "th3");

        // Start first thread immediately
        th1.start();


        // Start second thread(th2) once first thread(th1) is dead
        try {
            th1.join();
        }

        catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        th2.start();
        // Start third thread(th3) once second thread(th2) is dead

        try {
            th2.join();
        }

        catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        th3.start();
        // Displaying a message once third thread is dead

        try {
            th3.join();
        }

        catch (InterruptedException ie) {
            ie.printStackTrace();
        }

        System.out.println("All three threads have finished execution");
        */
        /*ENd of solution 15: write a program to
        impelment sequential thread execuation
        using join() method*/




        /*Question 16: write a program to
        impelment calling run method*/

        /*
        Thread th1 = new Thread(new RunMethodExample(), "th1");
        Thread th2 = new Thread(new RunMethodExample(), "th2");
        th1.run();
        th2.run();
        */
        /*END of solution 16: write a program to
        impelment calling run method*/




        /*question 17: write a program to impelment
        calling run method from the start()*/
        /*Thread th1 = new Thread(new RunMethodExample(), "th1");
        Thread th2 = new Thread(new RunMethodExample(), "th2");
        Thread th3 = new Thread(new RunMethodExample(), "th3");

        th1.start();
        th2.start();
        th3.start();*/
        /*question 17: write a program to impelment
        calling run method from the start()*/


        /*question-A*/
        /* RunnableThreadExample instance = new RunnableThreadExample();
        Thread thread = new Thread(instance);
        thread.start();

        while (instance.count != 5) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
            }
        }*/
        /*END of solution-A*/


        /*question-B*/
        /*ThreadExample instance = new ThreadExample();
        instance.start();

        while (instance.count != 5) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException exc) {
                exc.printStackTrace();
            }
        }*/
        /*END of solution-B*/


        /*question-C*/
        /*NoLockATM noLockATM = new NoLockATM();
        LockedATM lockedATM = new LockedATM();

        MyClass1 thread1 = new MyClass1(noLockATM, lockedATM);
        MyClass1 thread2 = new MyClass1(noLockATM, lockedATM);

        thread1.start();
        thread2.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        thread1.waitUntilDone();
        thread2.waitUntilDone();

        System.out.println("NoLock ATM: " + noLockATM.getBalance());
        System.out.println("Locked ATM: " + lockedATM.getBalance());
        int v = thread1.delta + thread2.delta + 100;
        System.out.println("Should Be: " + v);
        System.out.println("Program terminating.");*/
        /*END of solution-C*/


        /*question-D*/
        /*try {
            MyObject obj1 = new MyObject();
            MyObject obj2 = new MyObject();
            MyClass3 thread4 = new MyClass3(obj1, "1");
            MyClass3 thread5 = new MyClass3(obj2, "2");

            thread4.start();
            thread5.start();

            Thread.sleep(3000 * 3);
        }  catch (InterruptedException exc) {
            System.out.println("Program Interrupted.");
        }
        System.out.println("Program terminating.");*/
        /*END of solution-D*/



        /*question-E*/
        /*try {
            MyObject2 obj1 = new MyObject2();
            MyObject2 obj2 = new MyObject2();

            MyClass4 thread1 = new MyClass4(obj1, "1");
            MyClass4 thread2 = new MyClass4(obj1, "2");

            thread1.start();
            thread2.start();

            Thread.sleep(3000 * 3);
        } catch (InterruptedException exc) {
            System.out.println("Program Interrupted.");
        }*/
        /*END of solution-E*/


        /*question-F*/
        /*try {

            MyObject3 obj1 = new MyObject3();
            MyObject3 obj2 = new MyObject3();
            MyClass5 thread1 = new MyClass5(obj1, "1");
            MyClass5 thread2 = new MyClass5(obj1, "2");

            thread1.start();
            thread2.start();

            Thread.sleep(3000 * 3);
        } catch (InterruptedException exc) {
            System.out.println("Program Interrupted.");
        }
        System.out.println("Program terminating.");*/
        /*END of solution-F*/


        /*question-G*/

        /*END of solution-G*/


        /*question-H*/
        /*int[] res1 = {1, 2, 3, 4};
        int[] res2 = {1, 5, 4, 1};
        int[] res3 = {1, 4, 5};

        LockFactory.initialize(10);

        LockFactory lf = LockFactory.getInstance();
        System.out.println(lf.declare(1, res1));
        System.out.println(lf.declare(2, res2));
        System.out.println(lf.declare(3, res3));

        System.out.println(lf.getLock(1, 1));
        System.out.println(lf.getLock(1, 2));
        System.out.println(lf.getLock(2, 4));*/
        /*END of solution-H*/


        /*question-I*/
        /*FooBad foo = new FooBad();

        MyThread00 thread1 = new MyThread00(foo, "first");
        MyThread00 thread2 = new MyThread00(foo, "second");
        MyThread00 thread3 = new MyThread00(foo, "third");

        thread3.start();
        thread2.start();
        thread1.start();*/
        /*END of solution-I*/


        /*question-J*/
        /* Part 1 Demo -- same instance */
//        System.out.println("Part 1 Demo with same instance.");
//        Foo1 fooA = new Foo1("ObjectOne");
//        MyThread01 thread1a = new MyThread01(fooA, "Dog", "A");
//        MyThread01 thread2a = new MyThread01(fooA, "Cat", "A");
//        thread1a.start();
//        thread2a.start();
//        while (thread1a.isAlive() || thread2a.isAlive()) {
//            // some code
//        }
//        System.out.println("\n\n");

		/* Part 1 Demo -- difference instances */
//        System.out.println("Part 1 Demo with different instances.");
//        Foo1 fooB1 = new Foo1("ObjectOne");
//        Foo1 fooB2 = new Foo1("ObjectTwo");
//        MyThread01 thread1b = new MyThread01(fooB1, "Dog", "A");
//        MyThread01 thread2b = new MyThread01(fooB2, "Cat", "A");
//        thread1b.start();
//        thread2b.start();
//        while (thread1b.isAlive() || thread2b.isAlive()) {
//            // some code
//        }
//        System.out.println("\n\n");

		/* Part 2 Demo */
//        System.out.println("Part 2 Demo.");
//        Foo1 fooC = new Foo1("ObjectOne");
//        MyThread01 thread1c = new MyThread01(fooC, "Dog", "A");
//        MyThread01 thread2c = new MyThread01(fooC, "Cat", "B");
//        thread1c.start();
//        thread2c.start();
        /*END of solution-J*/


        System.out.println("Program terminating.");
    }
}

