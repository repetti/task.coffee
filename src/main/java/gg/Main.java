package gg;

/**
 * This is where everything starts
 *
 * Date: 19/10/15
 */
public class Main {

    public static void main(String[] args) {

        initLogger();
        MySystem system = new MySystem(new Coffee[]{
            new SimpleCoffee(Constants.TIME_FILLING_ESPRESSO, "espresso"),
                    new SimpleCoffee(Constants.TIME_FILLING_LATTE_MACCHIATO, "latte macchiato"),
                    new SimpleCoffee(Constants.TIME_FILLING_CAPPUCCINO, "cappuccino")
        }, Constants.QUANTITY_OF_PROGRAMMERS);

        system.start();
    }

    private static void initLogger() {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
    }
}
