package com.creditease.netspy.cupboard;

/**
 * Factory that provides the global {@link Cupboard} instance
 */
public final class CupboardFactory {
    private static Cupboard INSTANCE = new Cupboard();

    /**
     * Replace the Cupboard instance
     *
     * @param cupboard the instance to use
     */
    public static void setCupboard(Cupboard cupboard) {
        INSTANCE = cupboard;
    }

    public static Cupboard getInstance() {
        return INSTANCE;
    }

    public static Cupboard cupboard() {
        return INSTANCE;
    }
}
