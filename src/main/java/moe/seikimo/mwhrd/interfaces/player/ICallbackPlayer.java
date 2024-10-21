package moe.seikimo.mwhrd.interfaces.player;

import net.minecraft.world.World;

import java.util.function.Consumer;

public interface ICallbackPlayer {
    /**
     * Sets the callback for when the player's dimension changes.
     *
     * @param world The callback to run when the player's dimension changes.
     */
    void mwhrd$onDimensionChange(Consumer<World> world);
}
