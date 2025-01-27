package moe.seikimo.mwhrd.impl.script;

import moe.seikimo.mwhrd.impl.PlayerNpcElement;
import moe.seikimo.mwhrd.script.ScriptObject;

/**
 * Lua script wrapper around {@link PlayerNpcElement}.
 */
public record ScriptActor(PlayerNpcElement handle) implements ScriptObject {
    /**
     * Sets the actor as glowing or not.
     *
     * @param glowing True if the actor should be glowing, false otherwise.
     */
    public void setGlowing(boolean glowing) {
        this.handle.setGlowing(glowing);
    }
}
