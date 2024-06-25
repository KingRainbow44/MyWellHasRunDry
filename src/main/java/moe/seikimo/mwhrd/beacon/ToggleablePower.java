package moe.seikimo.mwhrd.beacon;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import moe.seikimo.mwhrd.utils.GUI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ToggleablePower extends BeaconPower {
    protected boolean enabled = true;

    public ToggleablePower(BlockPos blockPos) {
        super(blockPos);
    }

    /**
     * @return The effect's data.
     */
    protected abstract BeaconEffect getEffect();

    @Override
    public void read(World world, NbtCompound tag) {
        this.enabled = tag.getBoolean("enabled");
    }

    @Override
    public void write(World world, NbtCompound tag) {
        tag.putBoolean("enabled", this.enabled);
    }

    @Override
    public SimpleGui getGui(World world, PlayerEntity player) {
        return new Interface(this, (ServerPlayerEntity) player);
    }

    static final class Interface extends SimpleGui {
        private final ToggleablePower self;
        private final BeaconEffect data;

        public Interface(ToggleablePower self, ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X1, player, false);

            this.self = self;
            this.data = self.getEffect();

            this.setTitle(Text.literal(data.getDisplayName()));

            this.drawBorders();
            this.drawButtons();
        }

        private void drawBorders() {
            for (var i = 0; i < this.getSize(); i++) {
                this.setSlot(i, GUI.BORDER);
            }
        }

        private void drawButtons() {
            this.setSlot(4, new GuiElementBuilder(
                this.self.enabled ? Items.LIME_DYE : Items.GRAY_DYE
            )
                .setName(Text.literal("Toggle " + this.data.getDisplayName())
                    .formatted(this.self.enabled ? Formatting.GREEN : Formatting.GRAY))
                .addLoreLine(Text.empty())
                .addLoreLine(GUI.lore("Click to toggle this effect!", Formatting.YELLOW))
                .setCallback(() -> {
                    this.self.enabled = !this.self.enabled;
                    this.self.handle.mwhrd$save();

                    this.getPlayer().sendMessage(Text.literal("Toggled " + this.data.getDisplayName())
                        .formatted(this.self.enabled ? Formatting.GREEN : Formatting.RED), false);
                }));
        }
    }
}
