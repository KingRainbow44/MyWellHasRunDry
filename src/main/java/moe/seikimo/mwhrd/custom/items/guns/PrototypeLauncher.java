package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.CustomComponents;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Rarity;

public final class PrototypeLauncher extends BaseGun {
    public PrototypeLauncher(Settings settings) {
        super(
            settings
                .rarity(Rarity.UNCOMMON),
            new GunComponent(25, 25, 18, 2, 14),
            Items.IRON_HORSE_ARMOR
        );
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.SOUL_FIRE_FLAME;
    }
}
