package moe.seikimo.mwhrd.custom.items.guns;

import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Rarity;

public final class Frenzy extends BaseGun {
    public Frenzy(Settings settings) {
        super(
            settings
                .rarity(Rarity.UNCOMMON),
            new GunComponent(6, 40, 5, 0, 10),
            Items.DIAMOND_HORSE_ARMOR
        );
    }

    @Override
    public Item getMagazineItem() {
        return CustomItems.LIGHT_CRYSTAL;
    }

    @Override
    public int getMagazineCount() {
        return 3;
    }

    @Override
    public boolean isRapidFire() {
        return true;
    }

    @Override
    public SimpleParticleType getParticle() {
        return ParticleTypes.ASH;
    }

    @Override
    public int getParticleCount() {
        return 5;
    }
}
