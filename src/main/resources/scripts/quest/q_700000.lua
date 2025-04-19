quest = {
    id = 700000,
    conditions = {
        { type = "ALWAYS" }
    }
}

dialogue = {
    name = "Wandering Trader",
    text = {
        -- { locale = "<Minecraft text path>", delay = <delay in ticks until next> }
        { locale = "text.mwhrd.quest.700000.1", delay = 10 },
        { locale = "text.mwhrd.quest.700000.2", delay = 40 }
    }
}

--- Invoked before the dialogue starts playing.
--- @param _ ScriptContext
function before_dialogue(_) end

--- Invoked if the dialogue is already completed.
--- @param context ScriptContext
function already_completed(context)
    context.player.quests:reply("text.mwhrd.quest.700000.completed")
end

--- Invoked after the dialogue finishes playing.
--- @param context ScriptContext
function after_dialogue(context)
    context.player.quests:prompt(
        context,
        {
            { locale = "text.mwhrd.quest.700000.option.1", callback = "accept_quest" },
            { locale = "text.mwhrd.quest.700000.option.2", callback = "decline_quest" }
        }
    )
end

--- Invoked as a callback from a player prompt.
--- @param context ScriptContext
function accept_quest(context)
    context.player.quests:finishDialogue("text.mwhrd.quest.700000.accept")

    -- Complete the quest and unlock guild rivalry.
    context.player.quests:complete(700000)
end

--- Invoked as a callback from a player prompt.
--- @param context ScriptContext
function decline_quest(context)
    context.player.quests:reply("text.mwhrd.quest.700000.decline")
    context.player.quests:stopDialogue()
end
