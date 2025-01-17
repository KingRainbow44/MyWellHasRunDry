package moe.seikimo.mwhrd.custom;

import moe.seikimo.mwhrd.MyWellHasRunDry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public interface CustomDamageSources {
    RegistryKey<DamageType> GUNSHOT = RegistryKey.of(
        RegistryKeys.DAMAGE_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "gun")
    );

    RegistryKey<DamageType> RAILGUN_EXPLOSION = RegistryKey.of(
        RegistryKeys.DAMAGE_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "railgun_explosion")
    );

    /**
     * Damage source for a gunshot.
     */
    static DamageSource gunshot(World world) {
        return new DamageSource(
            world.getRegistryManager()
                .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                .getOrThrow(GUNSHOT),
            null, null
        );
    }

    /**
     * Damage source for a gunshot.
     */
    static DamageSource gunshot(World world, PlayerEntity attacker) {
        return new DamageSource(
            world.getRegistryManager()
                .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                .getOrThrow(GUNSHOT),
            attacker
        );
    }

    /**
     * Damage source for a railgun explosion.
     */
    static DamageSource railgunExplosion(World world) {
        return new DamageSource(
            world.getRegistryManager()
                .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                .getOrThrow(RAILGUN_EXPLOSION),
            null, null
        );
    }

    /**
     * Damage source for a railgun explosion.
     */
    static DamageSource railgunExplosion(World world, PlayerEntity attacker) {
        return new DamageSource(
            world.getRegistryManager()
                .getOrThrow(RegistryKeys.DAMAGE_TYPE)
                .getOrThrow(RAILGUN_EXPLOSION),
            attacker
        );
    }
}
