package moe.seikimo.mwhrd.utils;

public interface Ticks {
    /**
     * Converts hours to ticks.
     *
     * @param hours The duration in hours.
     * @return The duration in ticks.
     */
    static long ofHours(long hours) {
        return hours * 72000;
    }
}
