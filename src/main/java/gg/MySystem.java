package gg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main class, defining and controlling the whole system of programmers getting their coffee
 *
 * Date: 19/10/15
 */
public class MySystem {

    private static final Random r = new Random();
    private static final Logger log = LoggerFactory.getLogger(MySystem.class);

    private final Set<Coffee> coffees;

    private final BlockingQueue<CoffeeMachine> machinesQueue =
            new LinkedBlockingDeque<>(Constants.QUANTITY_OF_MACHINES * Constants.MAX_PROGRAMMERS_GET_PAID_COFFEE_PER_MACHINE);
    private final CountDownLatch start = new CountDownLatch(1);
    private final CountDownLatch done;
    private final ExecutorService programmersExecutor = Executors.newCachedThreadPool();
    private final List<Programmer> programmers;
    private final List<CoffeeMachine> coffeeMachines;
    private final Semaphore semaphoreChooseCoffee = new Semaphore(Constants.MAX_PROGRAMMERS_CHOOSE_COFFEE, true);
    private final Semaphore semaphorePayForCoffee = new Semaphore(Constants.MAX_PROGRAMMERS_PAY, true);
    private final Map<PaymentType, AtomicInteger> coffeeSold = new HashMap<>(PaymentType.values().length);

    public MySystem(Coffee[] coffees, int quantityOfProgrammers) {
        this.coffees = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(coffees)));
        done = new CountDownLatch(Constants.QUANTITY_OF_PROGRAMMERS);
        programmers = new LinkedList<>();
        for (int i = 0; i < quantityOfProgrammers; i++) {
            final Programmer programmer = new Programmer(
                    this,
                    coffees[r.nextInt(coffees.length)],
                    r.nextBoolean() ? PaymentType.CARD : PaymentType.CASH,
                    "Programmer " + i);
            programmers.add(programmer);
            programmersExecutor.submit(programmer);
        }
        coffeeMachines = new ArrayList<>();
        for (int i = 0; i < Constants.QUANTITY_OF_MACHINES; i++) {
            coffeeMachines.add(new CoffeeMachine(
                    Constants.MAX_PROGRAMMERS_GET_PAID_COFFEE_PER_MACHINE,
                    machinesQueue,
                    "CoffeeMachine " + i));
        }
        for (PaymentType pt : PaymentType.values()) {
            coffeeSold.put(pt, new AtomicInteger());
        }
    }

    public void start() {
        log.info("Started");
        long startNanos = System.nanoTime();
        start.countDown();
        try {
            log.info("waiting for threads to be done");
            done.await();
        } catch (InterruptedException e) {
            System.err.println("Failed to await programmers to get ready");
            e.printStackTrace();
        } finally {
            programmersExecutor.shutdown();
        }
        startNanos -= System.nanoTime();
        log.info("Total time: {}ms", -startNanos / 1_000_000);

        int coffeeSoldTotal = 0;
        for (PaymentType pt : PaymentType.values()) {
            int soldThis = coffeeSold.get(pt).get();
            log.info("{} cups of coffee were paid by {}", soldThis, pt);
            coffeeSoldTotal += soldThis;
        }
        log.info("{} cups of coffee sold", coffeeSoldTotal);

        for (CoffeeMachine cm : coffeeMachines) {
            log.info("### "); //this is just a delimiter ^^
            log.info("{}: Total coffee dispensed: {} cups", cm, cm.getCoffeeDispensed());
            for (Coffee c : coffees) {
                log.info("{}: Coffee '{}' dispensed: {} cups", cm, c.getName(), cm.getCoffeeDispensed(c));
            }
        }

        long fastestProgrammer = Long.MAX_VALUE;
        long slowestProgrammer = Long.MIN_VALUE;
        long avarageProgrammer = 0;
        for (Programmer p : programmers) {
            final long time = p.getTotalTime();
            if (time < fastestProgrammer) {
                fastestProgrammer = time;
            }
            if (time > slowestProgrammer) {
                slowestProgrammer = time;
            }
            avarageProgrammer += time;
        }
        avarageProgrammer /= Constants.QUANTITY_OF_PROGRAMMERS;
        log.info("Average programmer spent {}ms to get her coffee", avarageProgrammer);
        log.info("Fastest and slowest programmer spent {}ms and {}ms respectively to get her coffee",
                fastestProgrammer, slowestProgrammer);

    }

    public Set<Coffee> getAvailableCoffees() {
        return coffees;
    }

    public long getCoffee(Programmer programmer) {
        long startTime = 0;
        try {
            start.await();
            log.debug("{} started", programmer);
            startTime = System.nanoTime();
            semaphoreChooseCoffee.acquire();
            Coffee coffee;
            try {
                coffee = programmer.chooseCoffee(coffees);
            } finally {
                semaphoreChooseCoffee.release();
            }
            if (!coffees.contains(coffee)) {
                throw new RuntimeException("No such coffee. Sorry.");

            }
            log.debug("{} chose coffee", programmer);
            semaphorePayForCoffee.acquire();
            try {
                PaymentType paymentType = programmer.payForCoffee();
                coffeeSold.get(paymentType).incrementAndGet();
            } finally {
                semaphorePayForCoffee.release();
            }
            log.debug("{} payed", programmer);
            programmer.obtainCoffee(machinesQueue.take());
            log.debug("{} got coffee", programmer);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return 0;
        } finally {
            done.countDown();
            log.debug("{} programmers left", done.getCount());
        }
        return (System.nanoTime() - startTime) / 1000_000;
    }

}
