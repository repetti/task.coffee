package gg;

/**
 * Different payment types. They are just like different types of coffee, but they are unlikely to change...
 * One need long time to accept bitcoins, while new sorts of coffee can appear from nowhere. At least I think so.
 *
 * Date: 20/10/15
 */
public enum PaymentType {
    CASH(Constants.TIME_PAY_CASH), CARD(Constants.TIME_PAY_CARD);

    public final int payTime;

    PaymentType(int payTime) {
        this.payTime = payTime;
    }
}
