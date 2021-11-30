package DiningPhilosopher;

public class StarvingPhilosopher extends Philosopher {

    public StarvingPhilosopher(int id, Fork... forks) {
        super(id, forks);
    }

    public void acquire() {
        for (Fork fork : forks) {
            fork.P();
            System.out.println("Philosopher " + id + " acquired fork " + fork.id + ".");
        }
    }

    public void release() {
        for (Fork fork : forks) {
            System.out.println("Philosopher " + id + " released fork " + fork.id + ".");
            fork.V();
        }
    }

    public static void main(String args[]) {
        int numPhilosophers = 5;

        Fork[] forks = new Fork[numPhilosophers];
        for (int i = 0; i < numPhilosophers; i++) {
            forks[i] = new Fork(i);
        }

        Philosopher[] phils = new Philosopher[numPhilosophers];
        for (int i = 0; i < numPhilosophers; i++) {
            phils[i] = new StarvingPhilosopher(i, forks[i], forks[(i + 1) % numPhilosophers]);
            new Thread(phils[i]).start();
        }
    }
}
