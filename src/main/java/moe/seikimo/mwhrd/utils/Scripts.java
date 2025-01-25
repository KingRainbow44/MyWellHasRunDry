package moe.seikimo.mwhrd.utils;

import javax.script.Bindings;

public interface Scripts {
    /**
     * Converts a bindings object into a string.
     *
     * @param bindings The bindings object to convert.
     * @return A string representation of the bindings object.
     */
    static String toString(Bindings bindings) {
        var builder = new StringBuilder();

        for (var entry : bindings.entrySet()) {
            builder
                .append(entry.getKey())
                .append(" = ")
                .append(entry.getValue())
                .append("\n");
        }

        return builder.toString();
    }
}
