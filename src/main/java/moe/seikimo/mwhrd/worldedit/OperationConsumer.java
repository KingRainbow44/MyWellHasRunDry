package moe.seikimo.mwhrd.worldedit;

public interface OperationConsumer {
    /**
     * Invoked (usually) every 15 ticks.
     *
     * @param operation The parent operation.
     * @param frame The current frame. (incremented every 15 ticks)
     */
    void accept(Operation operation, int frame);
}
