package moe.seikimo.mwhrd.utils;

public final class Preconditions {
    private Preconditions() {
        // Prevent instantiation.
    }

    /**
     * Checks if the given object is not null.
     *
     * @param object The object to check.
     */
    public static void notNull(Object object) {
        if (object == null) {
            throw new NullPointerException("Object must not be null");
        }
    }
}
