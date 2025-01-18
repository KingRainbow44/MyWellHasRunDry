package moe.seikimo.mwhrd.script;

import lombok.extern.slf4j.Slf4j;

/**
 * A global instance of the scripting API.
 */
@Slf4j
public final class ScriptLib {
    /**
     * Logs an info message to the console.
     *
     * @param message The message to log.
     */
    public void info(String message) {
        log.info(message);
    }

    /**
     * Logs a warning message to the console.
     *
     * @param message The message to log.
     */
    public void warn(String message) {
        log.warn(message);
    }

    /**
     * Logs an error message to the console.
     *
     * @param message The message to log.
     */
    public void error(String message) {
        log.error(message);
    }
}
