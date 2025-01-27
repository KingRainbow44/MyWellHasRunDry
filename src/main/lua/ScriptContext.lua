--- @class ScriptLib
ScriptLib = {}

--- Teleports a player to a position.
--- @param player Player The player to teleport.
--- @param position Position The location to teleport the player to.
function ScriptLib:teleport(player, position) end

--- Logs a message to the server console.
--- The message will have the 'info' log level.
--- @param message string The message to log.
function ScriptLib:info(message) end

--- Logs a message to the server console.
--- The message will have the 'warn' log level.
--- @param message string The message to log.
function ScriptLib:warn(message) end

--- Logs a message to the server console.
--- The message will have the 'error' log level.
--- @param message string The message to log.
function ScriptLib:error(message) end

--- @class ScriptContext
--- @field actor Actor
--- @field player Player
--- @field interact$hand Hand
local ScriptContext = {}

--- @class Position
--- @field x number
--- @field y number
--- @field z number
--- @field pitch number
--- @field yaw number
--- @field dimension string
local Position = {}

--- @class Actor
local Actor = {}

--- Sets whether the actor is glowing.
--- @param glowing boolean Whether the actor should be glowing.
function Actor:setGlowing(glowing) end

--- @class Player
local Player = {}

--- Sends a message to the player.
--- @param message string The message to send.
function Player:message(message) end

--- Sends a translated message to the player.
--- @param key string The language key (from the language file) to send.
function Player:hint(key) end

--- @class Hand
--- @field MAIN_HAND number
--- @field OFF_HAND number
Hand = {
    MAIN_HAND = 0,
    OFF_HAND = 1
}
