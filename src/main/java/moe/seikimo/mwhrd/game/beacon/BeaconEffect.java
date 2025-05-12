package moe.seikimo.mwhrd.game.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import moe.seikimo.mwhrd.custom.CustomItems;
import moe.seikimo.mwhrd.game.beacon.powers.*;
import moe.seikimo.mwhrd.utils.Maps;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum BeaconEffect {
    // These are all effects which are applied but not displayed as a 'primary power'.
    DISABLE_SPAWNS("disable_spawns", "Disable Spawns", BeaconLevel.TIER_1, Items.ZOMBIE_SPAWN_EGG, SpawnControlPower::new, false, Items.AIR),
    UNBREAKING_TOOLS("unbreakable_tools", "Unbreakable Tools", BeaconLevel.TIER_2, Items.EXPERIENCE_BOTTLE, UnbreakableToolsPower::new, false, Items.AIR),
    EFFECTS("effects", "Effects", BeaconLevel.TIER_1, Items.POTION, EffectsPower::new, false, Items.AIR),

    // These are all effects which are displayed as a 'primary power'.
    PLOT_PURGER("plot_purger", "Plot Purger", BeaconLevel.TIER_4, Items.WITHER_SKELETON_SKULL, PlotPurgePower::new, true, CustomItems.PLOT_PURGER_UPGRADE),
    PIXEL_PRINTER("pixel_printer", "Pixel Printer", BeaconLevel.TIER_1, Items.CRAFTER, PixelPrinterPower::new, true, CustomItems.PIXEL_PRINTER_UPGRADE),
    FLIGHT_CRYSTAL("flight_crystal", "Flight Crystal", BeaconLevel.TIER_3, Items.ELYTRA, FlightPower::new, true, CustomItems.FLIGHT_CRYSTAL_UPGRADE),
    EYE_OF_TELEPORTATION("eye_of_teleportation", "Eye of Teleportation", BeaconLevel.TIER_2, Items.ENDER_EYE, TeleportationPower::new, true, CustomItems.TELEPORT_EYE_UPGRADE),
    WORLDEDIT("worldedit", "Builder's Grace", BeaconLevel.TIER_4, Items.WOODEN_AXE, WorldEditPower::new, true, CustomItems.WORLDEDIT_UPGRADE)
    ;

    public static final Map<Class<? extends BeaconPower>, BeaconEffect> POWERS = Maps.beaconPower()
        .put(SpawnControlPower.class, DISABLE_SPAWNS)
        .put(UnbreakableToolsPower.class, UNBREAKING_TOOLS)
        .put(EffectsPower.class, EFFECTS)
        .put(PlotPurgePower.class, PLOT_PURGER)
        .put(PixelPrinterPower.class, PIXEL_PRINTER)
        .put(FlightPower.class, FLIGHT_CRYSTAL)
        .put(TeleportationPower.class, EYE_OF_TELEPORTATION)
        .put(WorldEditPower.class, WORLDEDIT)
        .build();

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
    final BeaconLevel minLevel;
    final Item displayItem;
    final BeaconPower.Initializer callbacks;
    final boolean draw;
    final Item item;

    /**
     * @return A new instance of the BeaconPower.
     */
    public BeaconPower create(BlockPos blockPos) {
        return callbacks.create(blockPos);
    }

    /**
     * Method for blankly applying the effect to a player.
     *
     * @param world The world the player is in.
     * @param player The player to apply the effect to.
     */
    public void apply(World world, PlayerEntity player) {
        var power = this.create(BlockPos.ORIGIN);
        power.apply(world, 0, player);
    }

    /**
     * Method for blankly removing the effect from a player.
     *
     * @param world The world the player is in.
     * @param player The player to remove the effect from.
     */
    public void remove(World world, PlayerEntity player) {
        var power = this.create(BlockPos.ORIGIN);
        power.remove(world, player);
    }
}
