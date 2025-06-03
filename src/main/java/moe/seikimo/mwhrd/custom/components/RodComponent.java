package moe.seikimo.mwhrd.custom.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Component for fishing rods.
 *
 * @param durability An integer >= 0 representing the durability resistance of the rod.
 *                   A value of '0' means the rod will always take 1 damage per use.
 * @param strength An integer >= 0 representing the strength of the rod.
 *                 A value of '0' means there will be no reduction in difficulty when fishing.
 * @param quantity An integer > 0 representing the quantity of fish that can be caught at once.
 *                 A value of '0' means the rod will not catch any fish.
 *                 This value is always floored to the nearest integer.
 */
public record RodComponent(
    float durability,
    float strength,
    float quantity
) {
    public static final RodComponent EMPTY = new RodComponent(0, 0, 1);

    public static final Codec<RodComponent> CODEC = RecordCodecBuilder.create(builder ->
        builder.group(
            Codec.FLOAT.fieldOf("durability").forGetter(RodComponent::durability),
            Codec.FLOAT.fieldOf("strength").forGetter(RodComponent::strength),
            Codec.FLOAT.fieldOf("quantity").forGetter(RodComponent::quantity)
        ).apply(builder, RodComponent::new)
    );
}
