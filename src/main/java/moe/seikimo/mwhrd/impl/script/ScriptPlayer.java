package moe.seikimo.mwhrd.impl.script;

import moe.seikimo.mwhrd.script.ScriptObject;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public record ScriptPlayer(ServerPlayerEntity handle) implements ScriptObject {
    /**
     * Sends a literal message to the player.
     *
     * @param message The message to send.
     */
    public void sendMessage(String message) {
        this.handle.sendMessage(Text.literal(message), false);
    }

    /**
     * Sends a message to the player.
     * This message must be included in the language file for the mod.
     *
     * @param key The key of the message to send.
     */
    public void send(String key) {
        this.handle.sendMessage(Text.translatable(key), false);
    }
}
