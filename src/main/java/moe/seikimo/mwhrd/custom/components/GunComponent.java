package moe.seikimo.mwhrd.custom.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Component for guns.
 *
 * @param baseDamage The base damage of the gun.
 * @param maxAmmo The maximum ammo the gun can hold.
 * @param range The range of the gun.
 * @param fireRate The rate at which the gun can fire. (in ticks)
 * @param reloadTime The time it takes to reload the gun. (in ticks)
 */
public record GunComponent(
    int baseDamage,
    int maxAmmo,
    int range,
    int fireRate,
    int reloadTime
) {
    public static final GunComponent EMPTY = new GunComponent(0, 0, 0, 0, 0);

    public static final Codec<GunComponent> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            Codec.INT.fieldOf("baseDamage").forGetter(GunComponent::baseDamage),
            Codec.INT.fieldOf("maxAmmo").forGetter(GunComponent::maxAmmo),
            Codec.INT.fieldOf("range").forGetter(GunComponent::range),
            Codec.INT.fieldOf("fireRate").forGetter(GunComponent::fireRate),
            Codec.INT.fieldOf("reloadTime").forGetter(GunComponent::reloadTime)
        ).apply(builder, GunComponent::new)
    );
}
