package moe.seikimo.mwhrd.utils;

import java.security.SecureRandom;

public interface Random {
    SecureRandom RANDOM = new SecureRandom();

    /**
     * Returns true if a chance is met.
     *
     * @param chance A value between 0.0 and 1.0. (exclusive, inclusive)
     * @return True if the chance is met.
     */
    static boolean success(double chance) {
        return RANDOM.nextDouble() < chance;
    }
}
