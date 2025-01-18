The Lua scripting part of MWHRD is mostly taken from 'KingRainbow44/AltService', which was stolen from 'Grasscutters/Grasscutter'.

## Creating a Script

Scripts are written in Lua, and can be uploaded to any publicly available server.

```lua
-- The 'events' table is used to define event listeners, and their conditions.
events = {
    { event = EventType.DEBUG, condition = "cond_", action = "action_" }
}

function init(context)
    -- The 'init' function is called as the script is being loaded..
end

function start(context)
    -- The 'start' function is always called before the script begins.
end
```

## Using the Scripting API

The scripting API is defined in EmmyLua in `resources/ScriptLib.lua`.

### Functions

| Lua Function Name | Parameters       | Description                                                     | Example                                         |
|-------------------|------------------|-----------------------------------------------------------------|-------------------------------------------------|
| info              | `String message` | Prints a message to the logger of level 'info'.                 | ScriptLib.info("Hello world!")                  |
| warn              | `String message` | Prints a message to the logger of level 'warn'.                 | ScriptLib.warn("Oh no! Something broke...")     |
| error             | `String message` | Prints a message to the logger of level 'error'.                | ScriptLib.error("Something went REALLY wrong.") |
| print             | `any object`     | Serializes the given object to JSON, then writes it to the log. | ScriptLib.print({ test = "Hello!" })            |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
|                   |                  |                                                                 |                                                 |
