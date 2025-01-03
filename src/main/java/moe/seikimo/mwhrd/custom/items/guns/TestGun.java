package moe.seikimo.mwhrd.custom.items.guns;

import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;

public final class TestGun extends BaseGun {
    public TestGun(Settings settings) {
        super(
            settings
                .useCooldown(0.08f),
            Items.IRON_HORSE_ARMOR
        );
    }

    @Override
    public int getRange() {
        return 18;
    }

    @Override
    public float getDamage(int distance) {
        return Math.clamp(18 - distance, 1, 18);
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.SMALL_GUST;
    }
}
