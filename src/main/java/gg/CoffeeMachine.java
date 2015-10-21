package gg;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Date: 20/10/15
 */
public class CoffeeMachine {

    private static final Object superValue = new Object();

    private final Semaphore semaphore;

    private final ConcurrentMap<Programmer,Object> users = new ConcurrentHashMap<>(2);
    private final ConcurrentMap<Coffee,AtomicInteger> coffeeDispensed = new ConcurrentHashMap<>();
    private final BlockingQueue<CoffeeMachine> machines;
    private final String name;

    public CoffeeMachine(int maxUsers, BlockingQueue<CoffeeMachine> machines, String name) {
        this.name = name;
        this.semaphore = new Semaphore(maxUsers);
        this.machines = machines;
        for (int i = 0; i < maxUsers; i++) {
            machines.add(this);
        }
    }

    public Cup findACup(Programmer programmer) throws InterruptedException {
        semaphore.acquire();
        users.put(programmer, superValue);
        TimeUnit.MILLISECONDS.sleep(Constants.TIME_FIND_CUP);
        return new Cup(programmer);
    }

    public void putTheCupUnderTheOutlet(Cup cup) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Constants.TIME_PUT_CUP);

    }

    public void pickTheCoffee(Cup cup, Coffee coffee) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Constants.TIME_PICK_COFFEE);
        fillTheCup(cup, coffee);
    }

    private void fillTheCup(Cup cup, Coffee coffee) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(coffee.getPreparingTime());
        if (coffeeDispensed.containsKey(coffee)) {
            coffeeDispensed.get(coffee).incrementAndGet();
        } else {
            // not to create new AtomicInteger every time
            coffeeDispensed.putIfAbsent(coffee, new AtomicInteger()).incrementAndGet();
        }
        cup.fill(coffee);
    }

    public void getTheCup(Cup cup) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Constants.TIME_TAKE_CUP_AND_LEAVE);
        cancel(cup.getOwner());
    }

    public void cancel(Programmer programmer) {
        if (users.remove(programmer) == superValue) {
            semaphore.release();
            machines.add(this);
        }
    }

    public int getCoffeeDispensed() {
        int ret = 0;
        for (AtomicInteger i : coffeeDispensed.values()) {
            ret += i != null ? i.get() : 0;
        }
        return ret;
    }

    public int getCoffeeDispensed(Coffee coffee) {
        AtomicInteger i = coffeeDispensed.get(coffee);
        return i != null ? i.get() : 0;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
