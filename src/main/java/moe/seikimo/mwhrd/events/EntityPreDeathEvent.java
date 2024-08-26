package moe.seikimo.mwhrd.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

public interface EntityPreDeathEvent {
    /**
     * Event that is called before an entity dies.
     * <p>
     * If one listener calls for an entity not dying, the entity will not die.
     */
    Event<PreDeath> EVENT = EventFactory.createArrayBacked(
        PreDeath.class,
        (listeners) -> (entity, source) -> {
            var shouldDie = true;
            for (var listener : listeners) {
                if (!listener.onPreDeath(entity, source) && shouldDie) {
                    shouldDie = false;
                }
            }

            return shouldDie;
        }
    );

    @FunctionalInterface
    interface PreDeath {
        /**
         * Invoked before an entity dies.
         *
         * @param entity The entity who is about to die.
         * @param source The source dealing the final blow.
         * @return True if the entity should die.
         */
        boolean onPreDeath(LivingEntity entity, DamageSource source);
    }
}
