package moe.seikimo.mwhrd.mixin.mwhrd;

import moe.seikimo.mwhrd.interfaces.game.IRespawnableMob;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity implements IRespawnableMob {
    protected MobEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    /// <editor-fold desc="IRespawnableMob">

    @Unique private BlockPos spawnPoint = new BlockPos(0, 0, 0);

    @Override
    public void mwhrd$setSpawnPoint(BlockPos pos) {
        this.spawnPoint = pos;
    }

    @Override
    public BlockPos mwhrd$getSpawnPoint() {
        return this.spawnPoint;
    }

    /// </editor-fold>
}
