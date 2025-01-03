package moe.seikimo.mwhrd.custom.items.guns;

import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Rarity;

public final class PrototypeLauncher extends BaseGun {
    private static final int BASE_DAMAGE = 25;

    public PrototypeLauncher(Settings settings) {
        super(
            settings
                .useCooldown(0.0125f)
                .rarity(Rarity.UNCOMMON),
            Items.IRON_HORSE_ARMOR
        );
    }

    @Override
    public int getRange() {
        return 25;
    }

    @Override
    public float getDamage(int distance) {
        return Math.clamp(BASE_DAMAGE - distance / 4, 1, BASE_DAMAGE);
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.SOUL_FIRE_FLAME;
    }
}
