package gg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The person, who drinks coffee
 *
 * Date: 19/10/15
 */
public class Programmer implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Programmer.class);

    private final Coffee favoriteCoffee;
    private final PaymentType paymentType;
    private final MySystem system;
    private final String name;

    /**
     * Total time to get a cup of coffee in ms
     */
    private volatile long totalTime;

    public Programmer(MySystem system, Coffee favoriteCoffee, PaymentType paymentType, String name) {
        this.favoriteCoffee = favoriteCoffee;
        this.paymentType = paymentType;
        this.system = system;
        this.name = name;
    }

    @Override
    public void run() {
        totalTime = system.getCoffee(this);

    }

    public Coffee chooseCoffee(Set<Coffee> coffees) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(Constants.TIME_CHOOSE_COFFEE);
        Set<Coffee> coffeeSet = system.getAvailableCoffees();
        return favoriteCoffee;
    }

    public PaymentType payForCoffee() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(paymentType.payTime);
        return paymentType;
    }

    public void obtainCoffee(CoffeeMachine coffeeMachine) throws InterruptedException {
        try {
            Cup cup = coffeeMachine.findACup(this);
            log.debug("{} found a cup", this);
            coffeeMachine.putTheCupUnderTheOutlet(cup);
            log.debug("{} put a cup", this);
            coffeeMachine.pickTheCoffee(cup, favoriteCoffee);
            log.debug("{} picked the coffee", this);
            coffeeMachine.getTheCup(cup);
            log.debug("{} got a cup with coffee", this);

        } catch (Exception e) {
            coffeeMachine.cancel(this);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Total time to get a cup of coffee
     * @return time in ms
     */
    public long getTotalTime() {
        return totalTime;
    }
}
