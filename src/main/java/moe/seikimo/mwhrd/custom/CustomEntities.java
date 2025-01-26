package moe.seikimo.mwhrd.custom;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.entities.GuardianOfLight;
import moe.seikimo.mwhrd.utils.Utils;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomEntities {
    EntityType<GuardianOfLight> GUARDIAN_OF_LIGHT = Registry.register(
        Registries.ENTITY_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "guardian_of_light"),
        EntityType.Builder.create(GuardianOfLight::new, SpawnGroup.MONSTER)
            .build(Utils.entityKey("guardian_of_light"))
    );

    /**
     * No-op method to trigger the static block.
     */
    static void register() {
        PolymerEntityUtils.registerType(GUARDIAN_OF_LIGHT);
        FabricDefaultAttributeRegistry.register(GUARDIAN_OF_LIGHT, GuardianOfLight.createAttributes());
    }
}
