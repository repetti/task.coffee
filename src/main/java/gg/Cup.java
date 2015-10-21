package gg;

/**
 * Cup, that can be filled with coffee
 *
 * Date: 20/10/15
 */
public class Cup {

    private final Programmer owner;

    public Cup(Programmer owner) {
        this.owner = owner;
    }

    public void fill(Coffee coffee) {
        // the cup is being filled with fresh coffee
    }

    public Programmer getOwner() {
        return owner;
    }
}
