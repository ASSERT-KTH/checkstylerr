package GlobalSnapshot;

import utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Account implements Runnable {

    private int id;
    private int balance;

    private ArrayList<Account> payees;
    private HashMap<Account,Channel> outgoingChannel;
    private HashMap<Account,Channel> incomingChannel;

    private boolean receivedMarker;
    private int state;

    private Random rand;

    public Account(int id, int initialBalance) {
        this.id = id;
        this.balance = initialBalance;

        payees = new ArrayList<>();
        outgoingChannel = new HashMap<>();
        incomingChannel = new HashMap<>();

        receivedMarker = false;
        state = 0;

        rand = new Random();
    }

    public void addPayee(Account payee) {
        payees.add(payee);

        Channel channel = new Channel(this, payee);
        this.outgoingChannel.put(payee, channel);
        payee.incomingChannel.put(this, channel);
    }

    private void sendMoney(Account payee, Transaction transaction) {
        balance -= transaction.getAmount();
        outgoingChannel.get(payee).add(transaction);
    }

    private void receiveMoney(Account sender, Transaction transaction) {
        balance += transaction.getAmount();
    }

    private void sendMarker(Account receiver, Marker marker) {
        outgoingChannel.get(receiver).add(marker);
    }

    private void receiveMarker(Account sender, Marker marker) {
        if (!receivedMarker){
            receivedMarker = true;
            takeLocalSnapshot();
            for (Account account : payees)
                if (account == sender)
                    incomingChannel.get(account).stopRecording();
                else
                    incomingChannel.get(account).startRecording();
            for (Account account : payees) {
                sendMarker(account, marker);
            }
        } else if (sender != null) {
            incomingChannel.get(sender).stopRecording();
        }
    }

    private void takeLocalSnapshot() {
        state = balance;

        System.out.printf("State of account %d: %3d\n", id, state);
    }

    public void takeGlobalSnapshot() {
        System.out.printf("Account %d initiated a global snapshot!\n", id);

        Marker marker = new Marker();
        receiveMarker(null, marker);
    }

    @Override
    public void run() {
        while (true) {
            Util.sleep(500, 1000);
            if (rand.nextInt(100) < 20) {       // send transactions
                Transaction transaction = new Transaction(rand.nextInt(Math.min(20, balance)));
                sendMoney(getRandomAccount(), transaction);
            } else {                                  // handle an event from an arbitrary channel
                processEventFrom(getRandomAccount());
            }
        }
    }

    public int getId() {
        return id;
    }

    private void processEventFrom(Account sender) {
        Channel channel = incomingChannel.get(sender);
        Event event = channel.poll();
        if (event instanceof Transaction)
            receiveMoney(sender, (Transaction) event);
        if (event instanceof Marker)
            receiveMarker(sender, (Marker) event);
    }

    private Account getRandomAccount() {
        return payees.get(rand.nextInt(payees.size()));
    }
}
