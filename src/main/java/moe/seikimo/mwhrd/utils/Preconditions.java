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

    /**
     * Checks if the given object is not null, with a custom message.
     *
     * @param object The object to check.
     * @param message The message to include in the exception if the object is null.
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }
}
