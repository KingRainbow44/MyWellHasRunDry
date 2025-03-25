package moe.seikimo.mwhrd.game.quest.data;

import lombok.Data;

/**
 * A dialogue option.
 * <p>
 * To be deserialized from a Lua table, the following is required:
 * - a no-arguments constructor must exist
 * - every deserializable field must have a setter method
 */
@Data
public final class DialogueOption {
    public String locale, callback;
}
