package moe.seikimo.mwhrd.custom;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.components.GunComponent;
import moe.seikimo.mwhrd.custom.components.ReloadComponent;
import moe.seikimo.mwhrd.custom.components.RodComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public interface CustomComponents {
    ComponentType<GunComponent> GUN = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "gun"),
        ComponentType.<GunComponent>builder().codec(GunComponent.CODEC).build()
    );

    ComponentType<ReloadComponent> GUN_RELOAD = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "gun_reload"),
        ComponentType.<ReloadComponent>builder().codec(ReloadComponent.CODEC).build()
    );

    ComponentType<Integer> GUN_BULLETS = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(MyWellHasRunDry.MOD_ID, "gun_bullets"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    ComponentType<RodComponent> ROD = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        Identifier.of(MyWellHasRunDry.MOD_ID, "rod"),
        ComponentType.<RodComponent>builder().codec(RodComponent.CODEC).build()
    );

    /**
     * Registers all custom components.
     */
    static void register() {
        PolymerComponent.registerDataComponent(
            GUN, GUN_RELOAD, GUN_BULLETS, ROD
        );
    }
}
