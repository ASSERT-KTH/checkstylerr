package Multithreading.LockExperiment;

import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Chaklader on 1/15/17.
 */


/*

 Assignment Question:
 --------------------

1. Create inner classes: Company and Loop inside the main class
2. From Main create two objects of class Company
3. Start thread loop for 10 on those objects
4. While Company1 talks to Company2 – it locks an object. If at the same time – if Company2 wants to talk to Company1
then it says – Conflicting – Lock already exist. (Both companies are already in talk)

Lock():
=======
java.util.concurrent.locks. A lock is a thread synchronization mechanism like synchronized blocks except locks can be
more sophisticated than Java’s synchronized blocks. It is an interfaces and classes providing a framework for locking
and waiting for conditions that is distinct from built-in synchronization and monitors.

UnLock():
=========
UnLock() releases the lock on Object.

ReentrantLock():
================
A ReentrantLock is owned by the thread last successfully locking, but not yet unlocking it. A thread invoking lock will return,
successfully acquiring the lock, when the lock is not owned by another thread. The method will return immediately if the current
thread already owns the lock.

TryLock():
==========
TryLock() acquires the lock only if it is free at the time of invocation */



public class MyLockExperiment {

    // Class CrunchifyLoop
    static class Loop implements Runnable {

        private Company molomics;
        private Company google;

        public Loop(Company companyName1, Company companyName2) {
            this.molomics = companyName1;
            this.google = companyName2;
        }

        public void run() {
            Random random = new Random();
            // Loop 10
            for (int counter = 0; counter <= 10; counter++) {
                try {
                    Thread.sleep(random.nextInt(5));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                google.talking(molomics);
            }
        }
    }

    // Class Company
    static class Company {

        private final String companyName;

        // ReentrantLock: Creates an instance of ReentrantLock. This is equivalent to using ReentrantLock(false)
        private final Lock lock = new ReentrantLock();

        // Constructor
        public Company(String name) {
            this.companyName = name;
        }

        public String getName() {
            return this.companyName;
        }

        public boolean isTalking(Company companyName) {

            Boolean molomicsLock = false;
            Boolean googleLock = false;

            try {
                // tryLock: Acquires the lock only if it is free at the time of invocation.
                molomicsLock = this.lock.tryLock();
                googleLock = companyName.lock.tryLock();
            } finally {
                if (!(molomicsLock && googleLock)) {
                    if (molomicsLock) {
                        // unlock: Releases the lock.
                        this.lock.unlock();
                    }
                    if (googleLock) {
                        companyName.lock.unlock();
                    }
                }
            }

            return molomicsLock && googleLock;
        }

        public void talking(Company companyName) {

            // Check if Lock is already exist?
            if (isTalking(companyName)) {
                try {
                    System.out.format("I'm %s: talking to %s %n", this.companyName, companyName.getName());
                } finally {
                    lock.unlock();
                    companyName.lock.unlock();
                }
            } else {
                System.out.format("\tLock Situation ==> I'm %s: talking to %s, but it seems"
                        + " we are already talking. Conflicting. %n", this.companyName, companyName.getName());
            }
        }
    }

    public static void main(String[] args) {

        final Company molomics = new Company("Molomics");
        final Company google = new Company("Google");
        new Thread(new Loop(molomics, google)).start();
        new Thread(new Loop(google, molomics)).start();
    }
}
