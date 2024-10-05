package moe.seikimo.mwhrd.utils;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.util.Identifier;

public interface Attributes {
    EntityAttributeModifier ZERO = add(Identifier.of("mwhrd", "zero"), 0.0D);

    /**
     * Helper method to create a new entity attribute modifier.
     *
     * @param identifier The identifier of the modifier.
     * @param value The value of the modifier.
     * @return The new entity attribute modifier.
     */
    static EntityAttributeModifier add(Identifier identifier, double value) {
        return new EntityAttributeModifier(identifier, value, EntityAttributeModifier.Operation.ADD_VALUE);
    }
}
