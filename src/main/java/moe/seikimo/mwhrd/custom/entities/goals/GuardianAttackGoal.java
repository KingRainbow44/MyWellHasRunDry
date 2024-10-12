package moe.seikimo.mwhrd.custom.entities.goals;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.mob.PathAwareEntity;

public final class GuardianAttackGoal extends MeleeAttackGoal {
    public GuardianAttackGoal(PathAwareEntity mob) {
        super(mob, 0.6d, true);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected boolean canAttack(LivingEntity target) {
        return
            this.mob.getVisibilityCache().canSee(target) &&
            this.mob.isInAttackRange(target);
    }
}
