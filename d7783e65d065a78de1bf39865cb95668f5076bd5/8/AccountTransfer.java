package com.multi;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Chaklader on 3/15/18.
 */
public class AccountTransfer {

    private ReentrantLock lock = new ReentrantLock();

    public void transfer(Account from, Account to, Integer amount) {

        boolean transfer = false;

        try {
            if (lock.tryLock()) {
                System.out.println(Thread.currentThread().getName() + " says accuire lock");
                boolean flag = from.debit(amount);
                if (flag) {
                    to.credit(amount);
                }
                System.out.println(Thread.currentThread().getName() + " :: " + from.getName() + " says :: now balance is " + from.getBalance());
                System.out.println(Thread.currentThread().getName() + " :: " + to.getName() + " says :: now balance is " + to.getBalance());
                transfer = true;
            } else {
                System.out.println(Thread.currentThread().getName() + " says fail to accuire both lock Try again");
                transfer(from, to, amount);//try again
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (transfer) {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) {
        
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // 1st account
        Account firstAccount = new Account();
        firstAccount.setBalance(20000);
        firstAccount.setName("Shamik Mitra");
        
        // 2nd account
        Account secondAccount = new Account();
        secondAccount.setName("Samir Mitra");
        
        AccountTransfer transfer = new AccountTransfer();
        
        
        Runnable a = new Runnable() {

            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                transfer.transfer(firstAccount, secondAccount, 200);
                System.out.println(Thread.currentThread().getName() + " says :: Transfer successfull");
            }
        };
        
        Runnable b = new Runnable() {
        
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                transfer.transfer(secondAccount, firstAccount, 1000);
                System.out.println(Thread.currentThread().getName() + " says :: Transfer successfull");
            }
        };
        
        for (int i = 0; i < 4; i++) {
            executorService.submit(a);
            executorService.submit(b);
        }

        // close the pool
        executorService.shutdown();
    }
}






/*
 *
*/
class Account {


    private ReentrantLock implicitLock = new ReentrantLock();
    private String name;
    private Integer balance = 10000;

    public ReentrantLock getImplicitLock() {
        return implicitLock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer blance) {
        this.balance = blance;
    }

    public boolean debit(Integer amount) {
        if (amount > balance) {
            System.out.println(Thread.currentThread().getName() + " :: " + name + " says ::" + amount + " grater than current balance");
            return false;
        }
        balance = balance - amount;
        System.out.println(Thread.currentThread().getName() + " :: " + name + " says ::" + amount + " Debited Success Fully");
        return true;
    }

    public void credit(Integer amount) {
        balance = balance + amount;
        System.out.println(Thread.currentThread().getName() + " :: " + name + " says ::" + amount + " Credited Success Fully");
    }
}



