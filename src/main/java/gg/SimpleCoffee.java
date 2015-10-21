package gg;

/**
 * Just basic type of the drink. Nothing special.
 *
 * Date: 19/10/15
 */
public class SimpleCoffee implements Coffee {
    final int preparingTime;
    final String name;

    public SimpleCoffee(int preparingTime, String name) {
        this.preparingTime = preparingTime;
        this.name = name;
    }

    @Override
    public int getPreparingTime() {
        return preparingTime;
    }

    @Override
    public String getName() {
        return name;
    }
}
