quest = {
    id = 000000,
    accept_conditions = {
        { type = "" }
    },
    complete_conditions = {
        { type = "" }
    }
}

dialogue = {
    -- name = "",
    -- text = {
    --     { locale = "<Minecraft text path>", delay = <delay in ticks until next> }
    -- }
}

--- Invoked if the dialogue is already completed.
--- @param context ScriptContext
function already_completed(context) end

--- Invoked before the dialogue starts playing.
--- @param context ScriptContext
function before_dialogue(context) end

--- Invoked after the dialogue finishes playing.
--- @param context ScriptContext
function after_dialogue(context) end
