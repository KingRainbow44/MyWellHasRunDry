quest = {
    conditions = {
        { type = "PLAYER_HAS_NO_QUESTS" }
    }
}

dialogue = {
    name = "Adventurer",
    text = {
        -- { locale = "<Minecraft text path>", delay = <delay in ticks until next> }
        { locale = "text.mwhrd.quest.100000.1", delay = 20 },
        { locale = "text.mwhrd.quest.100000.2", delay = 30 },
        { locale = "text.mwhrd.quest.100000.3", delay = 10 },
        { locale = "text.mwhrd.quest.100000.4", delay = 20 },
        { locale = "text.mwhrd.quest.100000.5", delay = 20 },
        { locale = "text.mwhrd.quest.100000.6", delay = 20 }
    }
}
--- Invoked before the dialogue starts playing.
--- @param _ ScriptContext
function before_dialogue(_) end

--- Invoked after the dialogue finishes playing.
--- @param context ScriptContext
function after_dialogue(context)
    context.player.quests:prompt(
        { locale = "text.mwhrd.quest.100000.option.1", callback = "accept_quest" },
        { locale = "text.mwhrd.quest.100000.option.2", callback = "decline_quest" }
    )
end

--- Invoked as a callback from a player prompt.
--- @param context ScriptContext
function accept_quest(context)
    context.dialogue:finish("text.mwhrd.quest.100000.accept")

    -- Start the boss fight for the player.
    context.player.quests:complete(100000)
end

--- Invoked as a callback from a player prompt.
--- @param context ScriptContext
function decline_quest(context)
    context.dialogue:reply("text.mwhrd.quest.100000.decline")
    context.dialogue:stop()
end
