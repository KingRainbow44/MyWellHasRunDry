package moe.seikimo.mwhrd.worldedit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.world.ServerWorld;

@RequiredArgsConstructor
public final class Operation {
    @Getter private final ServerWorld world;
    private final OperationConsumer consumer;

    private int frame = 1; // Frame always starts at 1.

    /**
     * This is called by {@link AsyncPool} when it deems fit.
     */
    public void run() {
        this.consumer.accept(this, this.frame++);
    }
}
