package moe.seikimo.mwhrd.gui.item;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.general.MapBuilder;
import moe.seikimo.mwhrd.MyWellHasRunDry;
import moe.seikimo.mwhrd.custom.CustomStructures;
import moe.seikimo.mwhrd.utils.GUI;
import moe.seikimo.mwhrd.utils.Utils;
import moe.seikimo.mwhrd.utils.items.ItemNbt;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.gen.structure.Structure;
import net.minecraft.world.gen.structure.StructureKeys;

import java.util.ArrayList;
import java.util.Map;

public final class AtlasLookupGui extends SimpleGui {
    private static final Map<Item, RegistryKey<Structure>> STRUCTURES =
        MapBuilder.<Item, RegistryKey<Structure>>create()
            .put(Items.OAK_LOG, StructureKeys.VILLAGE_PLAINS)
            .put(Items.SANDSTONE, StructureKeys.VILLAGE_DESERT)
            .put(Items.ACACIA_LOG, StructureKeys.VILLAGE_SAVANNA)
            .put(Items.SNOW, StructureKeys.VILLAGE_SNOWY)
            .put(Items.SPRUCE_LOG, StructureKeys.VILLAGE_TAIGA)
            .put(Items.OAK_STAIRS, CustomStructures.LIGHT_SHACK)
            .put(Items.TRIAL_KEY, StructureKeys.TRIAL_CHAMBERS)
            .put(Items.LILY_PAD, StructureKeys.SWAMP_HUT)
            .put(Items.ENDER_EYE, StructureKeys.STRONGHOLD)
            .put(Items.SPRUCE_BOAT, StructureKeys.SHIPWRECK)
            .put(Items.OAK_BOAT, StructureKeys.SHIPWRECK_BEACHED)
            .put(Items.CRYING_OBSIDIAN, StructureKeys.RUINED_PORTAL)
            .put(Items.CROSSBOW, StructureKeys.PILLAGER_OUTPOST)
            .put(Items.PRISMARINE, StructureKeys.MONUMENT)
            .put(Items.MINECART, StructureKeys.MINESHAFT)
            .put(Items.TOTEM_OF_UNDYING, StructureKeys.MANSION)
            .put(Items.MOSSY_COBBLESTONE, StructureKeys.JUNGLE_PYRAMID)
            .put(Items.SNOW_BLOCK, StructureKeys.IGLOO)
            .put(Items.BLAZE_POWDER, StructureKeys.FORTRESS)
            .put(Items.SHULKER_SHELL, StructureKeys.END_CITY)
            .put(Items.TNT, StructureKeys.DESERT_PYRAMID)
            .put(Items.GOLD_BLOCK, StructureKeys.BASTION_REMNANT)
            .put(Items.CHEST, StructureKeys.BURIED_TREASURE)
            .put(Items.ECHO_SHARD, StructureKeys.ANCIENT_CITY)
            .build();

    private static final Map<RegistryKey<Structure>, String> STRUCTURE_NAMES =
        MapBuilder.<RegistryKey<Structure>, String>create()
            .put(StructureKeys.VILLAGE_PLAINS, "Plains Village")
            .put(StructureKeys.VILLAGE_DESERT, "Desert Village")
            .put(StructureKeys.VILLAGE_SAVANNA, "Savanna Village")
            .put(StructureKeys.VILLAGE_SNOWY, "Snow Village")
            .put(StructureKeys.VILLAGE_TAIGA, "Taiga Village")
            .put(CustomStructures.LIGHT_SHACK, "Light Shack")
            .put(StructureKeys.TRIAL_CHAMBERS, "Trial Chamber")
            .put(StructureKeys.SWAMP_HUT, "Witch's Hut")
            .put(StructureKeys.STRONGHOLD, "Stronghold")
            .put(StructureKeys.SHIPWRECK, "Shipwreck")
            .put(StructureKeys.SHIPWRECK_BEACHED, "Shipwreck (Beached)")
            .put(StructureKeys.RUINED_PORTAL, "Ruined Portal")
            .put(StructureKeys.PILLAGER_OUTPOST, "Pillager Outpost")
            .put(StructureKeys.MONUMENT, "Ocean Monument")
            .put(StructureKeys.MINESHAFT, "Mineshaft")
            .put(StructureKeys.MANSION, "Woodland Mansion")
            .put(StructureKeys.JUNGLE_PYRAMID, "Jungle Temple")
            .put(StructureKeys.IGLOO, "Igloo")
            .put(StructureKeys.FORTRESS, "Nether Fortress")
            .put(StructureKeys.END_CITY, "End City")
            .put(StructureKeys.DESERT_PYRAMID, "Desert Pyramid")
            .put(StructureKeys.BASTION_REMNANT, "Bastion")
            .put(StructureKeys.BURIED_TREASURE, "Buried Treasure")
            .put(StructureKeys.ANCIENT_CITY, "Ancient City")
            .build();

    /**
     * Opens the GUI for the player.
     *
     * @param atlas 'The Atlas' item stack.
     * @param player The player instance.
     */
    public static void open(ItemStack atlas, ServerPlayerEntity player) {
        new AtlasLookupGui(atlas, player).open();
    }

    private final ItemStack stack;

    /**
     * Constructs a new simple container gui for the supplied player.
     *
     * @param atlas 'The Atlas' item stack instance.
     * @param player               the player to server this gui to
     */
    public AtlasLookupGui(ItemStack atlas, ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.stack = atlas;

        this.setTitle(Text.literal("Atlas Lookup - Structures"));

        // Draw the GUI contents.
        GUI.drawBorders(this);
        this.drawButtons();
    }

    /**
     * Draws the buttons for the GUI.
     */
    private void drawButtons() {
        var list = new ArrayList<>(STRUCTURES.entrySet());
        GUI.drawBorderedList(this, list, element -> {
            var item = element.getKey();
            var structureKey = element.getValue();
            var structureName = STRUCTURE_NAMES.get(structureKey);

            return new GuiElementBuilder(item)
                .setName(Text.literal(structureName)
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to locate!")
                    .formatted(Formatting.YELLOW))
                .setCallback(() -> {
                    // Locate the nearest structure.
                    var structureRegistry = MyWellHasRunDry.getRegistry()
                        .getOrThrow(RegistryKeys.STRUCTURE);
                    var structure = structureRegistry.get(structureKey);
                    var structureEntry = structureRegistry.getEntry(structure);

                    if (!(this.getPlayer().getWorld() instanceof ServerWorld world)) {
                        return;
                    }

                    var result = world
                        .getChunkManager()
                        .getChunkGenerator()
                        .locateStructure(world, RegistryEntryList.of(structureEntry), player.getBlockPos(), 100, false);

                    // If no result was found, inform the player.
                    this.close();
                    if (result == null) {
                        this.getPlayer().sendMessage(Text.literal("No structure of that type nearby.")
                            .formatted(Formatting.RED));
                    } else {
                        var position = result.getFirst();
                        this.getPlayer().sendMessage(Text.literal("A %s was found at %s!"
                            .formatted(structureName, Utils.serialize(position)))
                            .formatted(Formatting.GREEN));

                        // Update the stack's NBT.
                        var nbt = ItemNbt.wrap(this.stack);
                        nbt.set("dest", new GlobalPos(world.getRegistryKey(), position));
                    }
                })
                .build();
        });
    }
}
