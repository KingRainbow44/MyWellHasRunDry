# My Well Has Run Dry

another content mod for Minecraft: Java Edition (and Minecraft: Bedrock Edition!)

## Features

See [the wiki](https://docs.seikimo.moe/mwhrd/features) for a full list of features.

## Cool Features

- [ ] World patching
  - `.patch` binary implementation for modifying worlds on the client
- [x] Quest system
  - Fully scriptable/data-driven quests with persistent state
- [ ] Economy & factions
  - A full implementation of microeconomics in Minecraft
  - Factions are clans which divide players into groups
- Full compatibility with Vanilla (and Bedrock!) clients
  - No mods are required to join the server
  - The mod can be added to **ANY** existing Minecraft world
  - As a side effect, Bedrock players are able to join and play on the server

## Technologies Used

- [Polymer](https://github.com/Patbox/polymer) - Registry hacks to allow Vanilla clients to join fully-modded servers
  - This is **not** what the mod/library actually does, but it is a good simplification of it.
- [LuaJ](https://github.com/luaj/luaj) - Scripting engine for quests/content
- [MongoDB](https://mongodb.com) and [Morphia](https://github.com/MorphiaOrg/morphia) - Database engine & object document mapper for storing mod-related data
  - [mongo-java-server](https://github.com/bwaldvogel/mongo-java-server) - Embedded MongoDB server for testing; used to replace a normal MongoDB database

## Credits

- The [Breath of the Wild in Minecraft](https://youtu.be/lmdl2Wu7PO0) Project
  - This is the world that makes the amazing scenery in _The Overworld Expanse_ possible.
