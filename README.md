<br/>
<div align="center">
<a href="https://github.com/ShaanCoding/ReadME-Generator">
<img src="./src/main/resources/icon.png" alt="Logo" width="80" height="80">
</a>
<h3 align="center">CasualChamionships</h3>
<p align="center">
The Minecraft mod used for CasualChampionship events!
</p>
</div>

## About The Project

CasualChampionships is a recurring event held by [Sensei](https://github.com/senseiwells) 
and [Santa](https://github.com/Super-Santa) for technical Minecraft servers.

Currently, CasualChampionships only consists of a single approximately 2 hour long 
UHC minigame, however, with plans to expand to adopt a more mcc-style format consisting 
of multiple shorter minigames.

### Built With

The majority of this mod relies upon the [arcade api](https://github.com/CasualChampionships/arcade)
which is a library designed for server-side fabric minigame development and is written in Kotlin.

## Getting Started

### Compiling

These are the steps for compiling CasualChampionships locally:

1. Clone the repo:
   ```sh
   git clone https://github.com/CasualChampionships/CasualChampionships.git
   ```
2. Build with Gradle:
   ```sh
   gradlew build
   ```
3. The mod will be built and located in your `build/libs` directory.

## Usage

### Installation

First, you will need a fabric server, follow 
[this guide](https://wiki.fabricmc.net/player:tutorials:install_server) 
if you do not have one already.

CasualChampionships also relies on the following mods, which you must have installed
separately:
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- [Placeholder API](https://modrinth.com/mod/placeholder-api)

### Configuration

Once you have all mods installed on your server, you can boot up your server
to generate the initial configuration files. 
These will be located in: `./config/casual-championships/`

In `config.json` is the configurations for syncing teams and stats with an
external database. 
This can be left blank (as default) and no syncing will happen.

The `packs` directory contains all additional and optional resource packs you
wish to use during the event, it also contains a subdirectory `generated` which
contains all the automatically generated resource packs used during the event,
you should not touch these.

The `event` directory contains the current event data for CasualChampionships.
You can modify `event.json`, by default, it will look like this:
```json
{
  "name": "default",
  "type": "arcade:simple", 
  "minigames": [], 
  "repeat": true,
  "operators": [],
  "lobby": {
    "type": "arcade:lobby"
  }
}
```
The full details of this config will not be explained due to its complexity,
but here are the basics. 

This file declares the lobby for the event as well as the minigames (in sequential order)
in which they will be played:
- `"name"` is the name of the event (used to identify the event when syncing to the database)
- `"type"` denotes the type of the event
- `"minigames"` a list of minigame definitions, these minigames will be played in-order
- `"repeat"` whether to repeat the minigames after reaching the end of the list
- `"operators"` a list of admins for the minigames
- `"lobby"` is a lobby minigame definition

Let's first configure the type of our event to be `"casual:championships"`:
```json5
{
  // ...
  "type": "casual:championships", 
  // ...
}
```

We should also configure the lobby to be the casual lobby, we can do this by
changing the following:
```json5
{
  // ...
  "lobby": {
    "type": "casual:lobby"
  },
  // ...
}
```

Now, if we want to add the UHC minigame to the rotation of minigames, we can add a
minigame definition:
```json5
{
  // ...
  "minigames": [
    {
      "type": "casual_uhc:uhc_minigame",
      "dimensions": {
        "minecraft:overworld": {
          "seed": 1234567890,
          "dimension": "casual:uhc_overworld"
        },
        "minecraft:the_nether": {
          "seed": 987654321,
          "dimension": "casual:uhc_nether"
        },
        "minecraft:the_end": {
          "dimension": "casual:uhc_end"
        }
      }
    }
  ],
  // ...
}
```
For the UHC minigame definition we define its type to be `"casual_uhc:uhc_minigame"`,
then we must specify its arguments which consist of the `"dimensions"` property.

Here you must define the seed and the underlying dimension key for each of the three
vanilla dimensions. 
You may leave the seed and/or the dimension key undefined, in which case a random seed
and random dimension id will be used respectively. 
Be warned: if you use a random dimension ids, the game will not be able to persist over a 
server restart! 

Once you have configured this you can run `/casual config reload` or restart the server
to allow these changes to take effect.

### Joining

Only players with `op` will be able to join the server initially. 
Admins should be given `op` and join the server first to ensure everything is working
correctly, after which they can open the floodgates to allow other players to join with
the `/casual floodgates open` command.
If you have set up a connected database, players will need to be added into that database
 to join.

### Settings

Now that we're in the lobby, we can configure the settings of the minigame we want to play.
We can run `/lobby next settings` to modify the settings of the next minigame.
If you have the UHC minigame as your next minigame you will be able to change settings such
as "Border Completion Time", "Grace Period", "Instant Smelt Ores", and much more.

From here you can run `/lobby ready teams` to check that players are ready, you can 
run `/lobby countdown` to start the countdown to begin the next minigame, or just
`/lobby start` to jump straight into the next minigame.

### Notes

If you plan on playing UHC, it is worth pre-generating the chunks in the dimensions you
have specified in your UHC minigame definition, especially if you are playing with many
players. 
We recommend using [Chunky](https://modrinth.com/plugin/chunky) for this.

## License

Distributed under the MIT Licence. 
See [MIT Licence](https://opensource.org/licenses/MIT) for more information.
