package moe.seikimo.mwhrd.impl.script;

import lombok.Getter;
import moe.seikimo.mwhrd.game.quest.PlayerQuestManager;
import moe.seikimo.mwhrd.interfaces.player.IStoryPlayer;
import moe.seikimo.mwhrd.script.ScriptObject;
import moe.seikimo.mwhrd.utils.Players;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class ScriptPlayer implements ScriptObject {
    private boolean initialized = false;

    @Getter
    private final ServerPlayerEntity handle;

    public PlayerQuestManager quests;

    public ScriptPlayer(ServerPlayerEntity handle) {
        this.handle = handle;
    }

    /**
     * Initializes the player.
     */
    public void initialize() {
        if (this.initialized) return;
        this.initialized = true;

        if (!(this.handle instanceof IStoryPlayer storyPlayer)) {
            throw new RuntimeException("Player needs to be a story player");
        }
        this.quests = storyPlayer.mwhrd$getQuestManager();
    }

    /**
     * Checks if the player is in a guild.
     *
     * @return {@code true} if the player is in a guild, {@code false} otherwise.
     */
    public boolean isInGuild() {
        return Players.getModel(this.handle).getGuild() != null;
    }

    /**
     * Sends a literal message to the player.
     *
     * @param message The message to send.
     */
    public void message(String message) {
        this.handle.sendMessage(Text.literal(message), false);
    }

    /**
     * Sends a message to the player.
     * This message must be included in the language file for the mod.
     *
     * @param key The key of the message to send.
     */
    public void hint(String key) {
        this.handle.sendMessage(Text.translatable(key), false);
    }
}
