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
local Position = {
    -- Special notes:

    -- The dimension field, when wrapped in `<>` will create an ephemeral dimension
    -- using the value in the brackets as the world type.
    -- The world type is sourced from `moe.seikimo.mwhrd.custom.CustomWorlds`.
}

--- @class Actor
local Actor = {}

--- Sets whether the actor is glowing.
--- @param glowing boolean Whether the actor should be glowing.
function Actor:setGlowing(glowing) end

--- @class Player
--- @field quests PlayerQuestManager
local Player = {}

--- Sends a message to the player.
--- @param message string The message to send.
function Player:message(message) end

--- Sends a translated message to the player.
--- @param key string The language key (from the language file) to send.
function Player:hint(key) end

--- @class PlayerQuestManager
local PlayerQuestManager = {}

--- Initiates dialogue with the player.
--- @param dialogue_id number The ID of the dialogue to start.
--- @overload fun(dialogue_id: number, is_quest: boolean)
function PlayerQuestManager:startDialogue(dialogue_id) end

--- Initiates dialogue with the player.
--- @param dialogue_id number The ID of the dialogue to start.
--- @param is_quest boolean Whether the dialogue is part of a quest.
function PlayerQuestManager:startDialogue(dialogue_id, is_quest) end

--- Marks a quest as complete for the player.
--- @param quest_id number The ID of the quest to complete.
function PlayerQuestManager:complete(quest_id) end

--- Prompts the player with a list of selectable options.
--- @param context ScriptContext The context of the script.
--- @param options table The options to prompt the player with.
function PlayerQuestManager:prompt(context, options) end

--- Marks the dialogue as finished.
--- Sends the translated message to the player.
--- @param key string The language key (from the language file) to send.
function PlayerQuestManager:finishDialogue(key) end

--- Unsets the player's current dialogue.
--- This is used if a conversation is over, but it hasn't been 'completed'.
function PlayerQuestManager:stopDialogue() end

--- Acts as a "reply" from the given actor.
--- Sends the translated message to the player.
--- @param key string The language key (from the language file) to send.
function PlayerQuestManager:reply(key) end

--- @class Hand
--- @field MAIN_HAND number
--- @field OFF_HAND number
Hand = {
    MAIN_HAND = 0,
    OFF_HAND = 1
}
