

// Yes, it's quite easy to achieve this with multiple threads, if you declare variable a as volatile.

// One thread constantly changes variable a from 1 to 3, and another thread constantly tests that a == 1 && a == 2 && a == 3. It happens often enough to have a continuous stream of "Success" printed on the console.

// (Note if you add an else {System.out.println("Failure");} clause, you'll see that the test fails far more often than it succeeds.)

// In practice, it also works without declaring a as volatile, but only 21 times on my MacBook. Without volatile, the compiler or HotSpot is allowed to cache a or replace the if statement with if (false). Most likely, HotSpot kicks in after a while and compiles it to assembly instructions that do cache the value of a. With volatile, it keeps printing "Success" forever.


// Can (a==1 && a==2 && a==3) evaluate to true in Java?
public class VolatileRace {

    private volatile int a;

    public void start() {
        new Thread(this::test).start();
        new Thread(this::change).start();
    }

    public void test() {
        while (true) {
            if (a == 1 && a == 2 && a == 3) {
                System.out.println("Success");
            }
        }
    }

    public void change() {
        while (true) {
            for (int i = 1; i < 4; i++) {
                a = i;
            }
        }
    }

    public static void main(String[] args) {
        new VolatileRace().start();
    }
}
