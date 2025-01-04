package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Rarity;

public final class TurfGun extends BaseGun {
    public TurfGun(Settings settings) {
        super(
            settings
                .rarity(Rarity.UNCOMMON),
            new GunComponent(13, 16, 11, 5, 10),
            Items.GOLDEN_HORSE_ARMOR
        );
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.END_ROD;
    }
}
