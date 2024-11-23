# v1.2.0 - The Rewrite!
Hey yall, 1.2.0 is here! 
Not too much to show in the changelog, as most of the changes were under the hood.
There is still some work that can be done, but I am happy where the mod is at currently.

As always, leave suggestions / bugs in the discord or github, and help localize the mod on [Crowdin](https://crowdin.com/project/oth3r-sit)!

Thank you for playing, and have a good day! - Oth3r 🦦

### Config
brand new config system for a better editing experience
* switch to json for all mod configuration
* split the config into 2, `server-config` & `sitting-config`
* block & item tag selection support
* select multiple block ids / tags per sitting height
* `sit-while-seated` config entry defaulted to false
* added a sitting block blacklist
* removed the YACL ModMenu config, as it is incompatible with the new system
* added a new custom config UI (full config control still WIP)

### Dismounting
*fixed dismounting sit entities!*
* added a new custom dismounting system
* dismounts the player in the direction that they are looking in, instead of on top of the block every time

### QOL
*im sure ive missed some things*
* interaction blocks now cannot be sat with the hand
* mod id is now `sit-oth3r` from `oth3r-sit`
* added keybinds
* sit toggle command
* `/sit` priorities target block over the block below the player
