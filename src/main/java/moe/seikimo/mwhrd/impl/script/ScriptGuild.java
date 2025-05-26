package moe.seikimo.mwhrd.impl.script;

import lombok.Getter;
import moe.seikimo.mwhrd.game.guilds.GuildInstance;
import moe.seikimo.mwhrd.script.ScriptObject;

public final class ScriptGuild implements ScriptObject {
    @Getter
    private final GuildInstance handle;

    public ScriptGuild(GuildInstance handle) {
        this.handle = handle;
    }
}
