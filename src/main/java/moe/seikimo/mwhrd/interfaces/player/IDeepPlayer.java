package moe.seikimo.mwhrd.interfaces.player;

import moe.seikimo.mwhrd.custom.entities.CelestialFishingBobberEntity;

public interface IDeepPlayer {
    /**
     * @return The player's fishing bobber, or null if the player is not fishing.
     */
    CelestialFishingBobberEntity mwhrd$getFishHook();

    /**
     * Sets the player's fishing bobber.
     *
     * @param fishHook The fishing bobber to set, or null to unset.
     */
    void mwhrd$setFishHook(CelestialFishingBobberEntity fishHook);
}
