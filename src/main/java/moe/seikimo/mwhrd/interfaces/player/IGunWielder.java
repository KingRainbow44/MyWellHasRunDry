package moe.seikimo.mwhrd.interfaces.player;

public interface IGunWielder {
    /**
     * Sets the fire cooldown.
     *
     * @param cooldown The cooldown in ticks.
     */
    void mwhrd$setCooldown(int cooldown);

    /**
     * @return True if the player can fire a gun.
     */
    boolean mwhrd$canFire();
}
