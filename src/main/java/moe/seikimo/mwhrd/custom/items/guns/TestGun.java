package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;

public final class TestGun extends BaseGun {
    public TestGun(Settings settings) {
        super(
            settings,
            new GunComponent(28, 100, 20, 1, 5),
            Items.IRON_HORSE_ARMOR
        );
    }

    @Override
    public Item getMagazineItem() {
        return CustomItems.MEDIUM_CRYSTAL;
    }

    @Override
    public boolean isRapidFire() {
        return true;
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.SMALL_GUST;
    }
}
