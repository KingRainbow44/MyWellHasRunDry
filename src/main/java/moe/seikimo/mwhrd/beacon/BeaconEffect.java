package moe.seikimo.mwhrd.beacon;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import moe.seikimo.mwhrd.beacon.powers.EffectsPower;
import moe.seikimo.mwhrd.beacon.powers.FlightPower;
import moe.seikimo.mwhrd.beacon.powers.PlotPurgePower;
import moe.seikimo.mwhrd.beacon.powers.TeleportationPower;
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
    DISABLE_SPAWNS("disable_spawns", "Disable Spawns", BeaconLevel.TIER_1, Items.TORCH, BeaconPower.Empty::new, false),
    UNBREAKING_TOOLS("unbreakable_tools", "Unbreakable Tools", BeaconLevel.TIER_2, Items.EXPERIENCE_BOTTLE, BeaconPower.Empty::new, false),
    EFFECTS("effects", "Effects", BeaconLevel.TIER_1, Items.POTION, EffectsPower::new, false),

    // These are all effects which are displayed as a 'primary power'.
    PLOT_PURGER("plot_purger", "Plot Purger", BeaconLevel.TIER_4, Items.WITHER_SKELETON_SKULL, PlotPurgePower::new, true),
    PIXEL_PRINTER("pixel_printer", "Pixel Printer", BeaconLevel.TIER_1, Items.CRAFTER, BeaconPower.Empty::new, true),
    FLIGHT_CRYSTAL("flight_crystal", "Flight Crystal", BeaconLevel.TIER_3, Items.ELYTRA, FlightPower::new, true),
    EYE_OF_TELEPORTATION("eye_of_teleportation", "Eye of Teleportation", BeaconLevel.TIER_2, Items.ENDER_EYE, TeleportationPower::new, true),
    WORLDEDIT("worldedit", "Builder's Grace", BeaconLevel.TIER_4, Items.WOODEN_AXE, BeaconPower.Empty::new, true)
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
    final BeaconLevel minLevel;
    final Item displayItem;
    final BeaconPower.Initializer callbacks;
    final boolean draw;

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
