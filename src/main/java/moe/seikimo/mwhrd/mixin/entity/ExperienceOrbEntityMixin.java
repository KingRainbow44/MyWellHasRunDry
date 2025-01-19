package moe.seikimo.mwhrd.mixin.entity;

import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin extends Entity {
    @Shadow private int amount;

    @Shadow private int pickingCount;

    public ExperienceOrbEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    /**
     * @author KingRainbow44
     * @reason Use {@link Players#addExperience(ServerPlayerEntity, int)} instead.
     */
    @Overwrite
    public void onPlayerCollision(PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return;
        }

        if (player.experiencePickUpDelay == 0) {
            player.experiencePickUpDelay = 1;
            player.sendPickup(this, 1);

            Players.addExperience(serverPlayer, this.amount);

            --this.pickingCount;
            if (this.pickingCount == 0) {
                this.discard();
            }
        }
    }
}
