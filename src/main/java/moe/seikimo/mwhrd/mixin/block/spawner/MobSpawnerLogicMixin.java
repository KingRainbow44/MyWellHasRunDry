package moe.seikimo.mwhrd.mixin.block.spawner;

import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(MobSpawnerLogic.class)
public abstract class MobSpawnerLogicMixin {
    private static final Set<EntityType<?>> BLACKLISTED = Set.of(
        EntityType.WITHER,
        EntityType.ENDER_DRAGON
    );

    @Shadow
    private @Nullable MobSpawnerEntry spawnEntry;

    @Inject(method = "getSpawnEntry", at = @At("HEAD"), cancellable = true)
    public void getSpawnEntry(World world, Random random, BlockPos pos, CallbackInfoReturnable<MobSpawnerEntry> cir) {
        if (this.spawnEntry == null) {
            return;
        }

        var nbt = this.spawnEntry.getNbt();
        var entityId = nbt.getString("id", null);
        if (entityId == null) {
            return;
        }

        var identifier = Identifier.of(entityId);
        var entityType = Registries.ENTITY_TYPE.get(identifier);

        if (BLACKLISTED.contains(entityType)) {
            cir.setReturnValue(new MobSpawnerEntry());
        }
    }
}
