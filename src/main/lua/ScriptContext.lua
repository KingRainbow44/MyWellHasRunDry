--- @class ScriptLib
ScriptLib = {}

--- Teleports a player to a position.
--- @param player Player The player to teleport.
--- @param position Position The location to teleport the player to.
function ScriptLib:teleport(player, position) end

--- @class ScriptContext
--- @field player Player
local ScriptContext = {}

--- @class Position
--- @field x number
--- @field y number
--- @field z number
--- @field pitch number
--- @field yaw number
--- @field dimension string
local Position = {}

--- @class Player
local Player = {}

--- Sends a message to the player.
--- @param message string The message to send.
function Player:message(message) end

--- Sends a translated message to the player.
--- @param key string The language key (from the language file) to send.
function Player:hint(key) end
