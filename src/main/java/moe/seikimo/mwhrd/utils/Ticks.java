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

    /**
     * Converts minutes to ticks.
     *
     * @param minutes The duration in minutes.
     * @return The duration in ticks.
     */
    static long ofMinutes(long minutes) {
        return minutes * 1200;
    }

    /**
     * Converts seconds to ticks.
     *
     * @param seconds The duration in seconds.
     * @return The duration in ticks.
     */
    static long ofSeconds(long seconds) {
        return seconds * 20;
    }
}
