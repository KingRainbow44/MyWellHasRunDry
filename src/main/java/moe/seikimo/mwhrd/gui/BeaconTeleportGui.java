package moe.seikimo.mwhrd.gui;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.beacon.BeaconEntry;
import moe.seikimo.mwhrd.beacon.BeaconManager;
import moe.seikimo.mwhrd.interfaces.IPlayerConditions;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static moe.seikimo.mwhrd.utils.GUI.BORDER;

public final class BeaconTeleportGui extends SimpleGui {
    private static final int CENTER = 22;
    private static final int BACK = 48;
    private static final int NEXT = 50;

    private static final int PER_PAGE = 28;

    private int baseIndex = 0;

    public BeaconTeleportGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);

        this.setTitle(Text.literal("Beacon Teleportation"));

        GUI.drawBorders(this);
        this.drawWaypoints();
        this.drawArrows();
    }

    @Override
    public void onClose() {
        var condPlayer = (IPlayerConditions) this.getPlayer();
        condPlayer.mwhrd$setClosedCooldown(System.currentTimeMillis() + 3000);
    }

    /**
     * Draws all active waypoints.
     */
    private void drawWaypoints() {
        BeaconManager.purge();

        var waypoints = BeaconManager.getTpBeacons();
        if (waypoints.isEmpty()) {
            this.setSlot(CENTER, new GuiElementBuilder(Items.BARRIER)
                .setName(Text.literal("No waypoints found!")
                    .formatted(Formatting.RED))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Maybe ask someone to open their base?")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY)));
            return;
        }

        var waypointIndex = this.baseIndex;
        var slotIndex = 0;

        var keys = this.sortWaypoints(waypoints);
        for (var i = 0; i < PER_PAGE; i++) {
            var slot = GUI.indexToSlot(slotIndex++);
            if (waypointIndex >= keys.size()) {
                this.setSlot(slot, ItemStack.EMPTY);
                continue;
            }

            var key = keys.get(waypointIndex++);
            var waypoint = waypoints.get(key);

            var pos = waypoint.teleportTo();
            this.setSlot(slot, new GuiElementBuilder(Items.BEACON)
                .setName(Text.literal(waypoint.name())
                    .formatted(Formatting.GREEN))
                .addLoreLine(Text.literal("X: %s, Y: %s, Z: %s"
                    .formatted(pos.getX(), pos.getY(), pos.getZ()))
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.GRAY))
                .addLoreLine(Text.empty())
                .addLoreLine(Text.literal("Click to teleport!")
                    .setStyle(GUI.CLEAR)
                    .formatted(Formatting.YELLOW))
                .setCallback(() -> {
                    BeaconManager.teleport(key, this.getPlayer());
                    this.close();
                }));
        }
    }

    /**
     * Sorts the waypoints by distance from the player.
     *
     * @param waypoints The waypoints to sort.
     * @return The sorted waypoints.
     */
    private List<BlockPos> sortWaypoints(Map<BlockPos, BeaconEntry> waypoints) {
        var keys = new ArrayList<>(waypoints.keySet());
        // Sort the keys by distance from the player.
        // The closest to playerPos should show up first.
        var playerPos = this.getPlayer().getPos();
        keys.sort((a, b) -> {
            var posA = waypoints.get(a).teleportTo();
            var posB = waypoints.get(b).teleportTo();

            var distA = Vec3d.of(posA).distanceTo(playerPos);
            var distB = Vec3d.of(posB).distanceTo(playerPos);

            return Double.compare(distA, distB);
        });
        return keys;
    }

    /**
     * Draws the arrow buttons.
     */
    private void drawArrows() {
        BeaconManager.purge();

        var waypoints = BeaconManager.getTpBeacons();
        if (waypoints.isEmpty()) {
            this.setSlot(BACK, BORDER);
            this.setSlot(NEXT, BORDER);
            return;
        }
        if (waypoints.size() <= PER_PAGE) {
            this.setSlot(BACK, BORDER);
            this.setSlot(NEXT, BORDER);
            return;
        }

        // Draw arrows.
        if (this.baseIndex > 0) {
            this.setSlot(BACK, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Previous Page")
                    .formatted(Formatting.GREEN))
                .setCallback(() -> {
                    this.baseIndex -= PER_PAGE;
                    this.drawWaypoints();
                    this.drawArrows();
                }));
        } else {
            this.setSlot(BACK, BORDER);
        }

        if (this.baseIndex + PER_PAGE < waypoints.size()) {
            this.setSlot(NEXT, new GuiElementBuilder(Items.ARROW)
                .setName(Text.literal("Next Page")
                    .formatted(Formatting.GREEN))
                .setCallback(() -> {
                    this.baseIndex += PER_PAGE;
                    this.drawWaypoints();
                    this.drawArrows();
                }));
        } else {
            this.setSlot(NEXT, BORDER);
        }
    }
}
