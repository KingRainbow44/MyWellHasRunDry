package moe.seikimo.mwhrd.gui.guild;

import eu.pb4.sgui.api.gui.SignGui;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public final class GuildBankNameGui extends SignGui {
    private final Consumer<String> consumer;

    /**
     * Constructs a new instance of the guild bank/storage GUI.
     *
     * @param player The player to open the GUI for.
     */
    public GuildBankNameGui(ServerPlayerEntity player, Consumer<String> name) {
        super(player);

        this.consumer = name;

        this.setLine(1, Text.literal("^^^^^^^^"));
        this.setLine(2, Text.literal("Input a new name"));
        this.setLine(3, Text.literal("for the page"));
    }

    @Override
    public void onClose() {
        this.consumer.accept(this.getLine(0).getString());
    }
}
