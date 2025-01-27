positions = {
    --- @type Position
    starting_shrine = {
        x = 4089.5,
        y = 125,
        z = 5631.5,
        pitch = 180,
        yaw = 180,
        dimension = "mwhrd:overworld_expanse"
    }
}

--- @param context ScriptContext
function start_quest(context)
    ScriptLib:teleport(context.player, starting_shrine)
end
