package moe.seikimo.mwhrd.interfaces;

public interface IPlayerConditions {
    void mwhrd$setOminous(boolean ominous);
    boolean mwhrd$isOminous();

    void mwhrd$setInTrialChamber(boolean inTrialChamber);
    boolean mwhrd$isInTrialChamber();

    void mwhrd$setClosedCooldown(long until);
    long mwhrd$getClosedCooldown();

    void mwhrd$setUnbreakable(boolean unbreakable);
    boolean mwhrd$isUnbreakable();

    boolean mwhrd$isHardcore();
}
