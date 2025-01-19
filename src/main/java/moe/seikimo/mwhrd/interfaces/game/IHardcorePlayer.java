package moe.seikimo.mwhrd.interfaces.game;

import net.minecraft.entity.mob.MobEntity;

public interface IHardcorePlayer {
    /**
     * @return Whether the player is in hardcore mode.
     */
    boolean mwhrd$isKamikaze();

    /**
     * @return Whether the player has targets.
     */
    boolean mwhrd$hasTargets();

    /**
     * Adds a target to the player.
     *
     * @param target The target to add.
     */
    void mwhrd$addTarget(MobEntity target);
}
