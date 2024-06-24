package moe.seikimo.mwhrd.worldedit;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import moe.seikimo.mwhrd.utils.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public final class AsyncPool {
    private static final long TICKS = 10;
    private static final int BLOCKS_PER_OP = 2_500;

    @Getter private static AsyncPool instance;

    /**
     * Creates an async pool.
     */
    public static void initialize() {
        AsyncPool.instance = new AsyncPool();
        log.info("Created WorldEdit operation pool.");
    }

    private final BlockingQueue<Operation> operations
        = new LinkedBlockingQueue<>();

    private Operation currentOperation;

    private long ticks = 0;
    private long operationId = 0;

    private AsyncPool() {
        ServerTickEvents.START_SERVER_TICK.register(this::onTick);
    }

    /**
     * Invoked once at the start of every server tick.
     */
    private void onTick(MinecraftServer server) {
        if (this.ticks++ % TICKS == 0) {
            var operation = this.currentOperation;
            if (operation == null) {
                operation = this.currentOperation = this.operations.poll();
            }

            if (operation != null) {
                server.execute(operation::run);
            }
        }
    }

    /**
     * Clears the current operation.
     */
    public void clearOperation() {
        this.currentOperation = null;
    }

    /**
     * Fills blocks at two points to a certain block.
     *
     * @param executor The player which executed the operation.
     * @param world The world to execute the operation.
     */
    public CompletableFuture<List<BlockState>> fill(
        PlayerEntity executor, ServerWorld world,
        BlockPos first, BlockPos second, BlockState to
    ) {
        var operationId = this.operationId++;
        log.info("[ID: {}] {} executed a fill ({}) operation from {} to {} in {}.",
            operationId,
            executor.getName(), to.getBlock().getName().getString(),
            Utils.serialize(first), Utils.serialize(second),
            world.getDimensionEntry().getIdAsString());

        // Calculate every block position.
        var blocks = Utils.rectangle(first, second);

        var changedBlocks = new AtomicReference<List<BlockState>>(
            Collections.synchronizedList(new ArrayList<>())
        );
        var future = new CompletableFuture<List<BlockState>>();

        var handler = AsyncPool.fillConsumer(
            world, to, blocks, future, changedBlocks);
        future.whenComplete((changed, exception) -> {
            this.clearOperation();
        });

        var result = this.operations.offer(new Operation(world, handler));
        if (!result) {
            log.warn("Failed to create operation {}.", operationId);
        }

        return future;
    }

    private static OperationConsumer fillConsumer(
        ServerWorld world, BlockState to, List<BlockPos> blocks,
        CompletableFuture<List<BlockState>> future, AtomicReference<List<BlockState>> changed
    ) {
        var framesRequired = Math.ceil((double) blocks.size() / BLOCKS_PER_OP);
        var startIndex = new AtomicInteger(0);

        var blocksChanged = changed.get();
        return (op, frame) -> {
            var need = frame == framesRequired ?
                blocks.size() % BLOCKS_PER_OP :
                BLOCKS_PER_OP;
            for (var i = 0; i < need; i++) {
                var index = i + startIndex.get();
                if (index >= blocks.size()) {
                    log.warn("Index out of bounds: {} > {}.", index, blocks.size());
                    log.warn("need: {}; frame: {}; framesRequired: {}.",
                        need, frame, framesRequired);
                    break;
                }

                var block = blocks.get(index);
                var currentState = world.getBlockState(block);

                blocksChanged.add(currentState);
                world.setBlockState(block, to);
            }
            startIndex.addAndGet(need);

            if (frame >= framesRequired) {
                future.complete(blocksChanged);
            } else {
                changed.set(blocksChanged);
            }
        };
    }
}
