package moe.seikimo.mwhrd.game.quest.data;

import lombok.Data;
import net.minecraft.text.Text;

/**
 * A line of dialogue.
 * <p>
 * To be deserialized from a Lua table, the following is required:
 * - a no-arguments constructor must exist
 * - every deserializable field must have a setter method
 */
@Data
public final class DialogueLine {
    public String locale;
    public int delay;

    /**
     * @return The resolved text.
     */
    public Text resolve() {
        return Text.translatable(this.locale);
    }
}
