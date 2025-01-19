package moe.seikimo.mwhrd.interfaces.player;

public interface IHardcorePlayer {
    /**
     * @return The amount of ticks the player has been alive this session.
     */
    long mwhrd$getSessionTicks();

    /**
     * Resets the player's session ticks.
     */
    void mwhrd$resetSessionTicks();
}
