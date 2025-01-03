package moe.seikimo.mwhrd.custom.items.guns;

import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Rarity;

public final class TurfGun extends BaseGun {
    private static final int BASE_DAMAGE = 13;

    public TurfGun(Settings settings) {
        super(
            settings
                .useCooldown(0.25f)
                .rarity(Rarity.UNCOMMON),
            Items.GOLDEN_HORSE_ARMOR
        );
    }

    @Override
    public int getRange() {
        return 14;
    }

    @Override
    public float getDamage(int distance) {
        return Math.clamp(BASE_DAMAGE - distance / 2.5f, 1, BASE_DAMAGE);
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.END_ROD;
    }
}
