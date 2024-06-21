package moe.seikimo.mwhrd.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.Item;
import net.minecraft.item.Items;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum BeaconEffect {
    DISABLE_SPAWNS("disable_spawns", "Disable Spawns", Items.TORCH, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY),
    UNBREAKING_TOOLS("unbreakable_tools", "Unbreakable Tools", Items.EXPERIENCE_BOTTLE, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY),
    PLOT_PURGER("plot_purger", "Plot Purger", Items.WITHER_SKELETON_SKULL, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY),
    PIXEL_PRINTER("pixel_printer", "Pixel Printer", Items.CRAFTER, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY),
    FLIGHT_CRYSTAL("flight_crystal", "Flight Crystal", Items.ELYTRA, FlightCrystalBuff::apply, FlightCrystalBuff::remove),
    EYE_OF_TELEPORTATION("eye_of_teleportation", "Eye of Teleportation", Items.ENDER_EYE, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY),
    WORLDEDIT("worldedit", "Builder's Grace", Items.WOODEN_AXE, BeaconCallback.APPLY_EMPTY, BeaconCallback.REMOVE_EMPTY)
    ;

    private static final Map<String, BeaconEffect> idMap = new HashMap<>();

    static {
        Arrays.stream(BeaconEffect.values())
            .forEach(effect -> idMap.put(effect.id, effect));
    }

    /**
     * Get a BeaconEffect by its ID.
     */
    public static BeaconEffect getById(String id) {
        return idMap.get(id);
    }

    final String id;
    final String displayName;
    final Item displayItem;
    final BeaconCallback.Apply applyCallback;
    final BeaconCallback.Remove removeCallback;
}
