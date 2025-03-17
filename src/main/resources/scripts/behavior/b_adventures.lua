--- Invoked when the player interacts with the NPC.
--- @param context ScriptContext
function on_interact(context)
    context.player.quests:startDialogue(100000)
end
