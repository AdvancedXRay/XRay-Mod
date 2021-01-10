![XRay Logo](.github/assets/xray-forge-logo.svg)
# Advanced XRay (Forge Edition)

Minecraft Forge based XRay mod designed to aid players who don't like the ore searching process.

[![Forge Downloads](http://cf.way2muchnoise.eu/advanced-xray.svg)](https://www.curseforge.com/minecraft/mc-mods/advanced-xray) [![Forge Available For](http://cf.way2muchnoise.eu/versions/advanced-xray.svg)](https://www.curseforge.com/minecraft/mc-mods/advanced-xray)

[![GitHub license](https://img.shields.io/github/license/MichaelHillcox/XRay-Mod)](https://github.com/MichaelHillcox/XRay-Mod/blob/main/LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/MichaelHillcox/XRay-Mod)](https://github.com/MichaelHillcox/XRay-Mod/stargazers)
[![GitHub issues](https://img.shields.io/github/issues/MichaelHillcox/XRay-Mod)](https://github.com/MichaelHillcox/XRay-Mod/issues)
![GitHub all releases](https://img.shields.io/github/downloads/michaelhillcox/xray-mod/total)
![GitHub release (latest by date)](https://img.shields.io/github/v/release/michaelhillcox/xray-mod)
![GitHub Release Date](https://img.shields.io/github/release-date/michaelhillcox/xray-mod)
![GitHub last commit](https://img.shields.io/github/last-commit/michaelhillcox/xray-mod)
[![Build Status](https://ci.mikey.pro/buildStatus/icon?job=XRay-Mod%2Fmain)](https://ci.mikey.pro/job/XRay-Mod/job/main/)

Looking for the Fabric version? Click the button below :tada:

<a href="https://github.com/michaelhillcox/xray-fabric"><img src=".github/assets/xray-fabric-badge.svg" alt="drawing" width="250"/>
</a>

## Feature

- Built using Forge ⚒
- Clean UI For Adding, Deleting and Editing the blocks you want to X-Ray
- Full RGB Colour selector
- Searchable List to find Blocks
- Add Blocks from your hand
- Add Blocks you're looking at! 
- Searchable list of blocks you've added
- Json store for the blocks you've added. Easy to edit and share!

## Todo

- Mob support
- Support for all fluids now it's a new system in 1.14.

## How to use

**Using XRay**

*Please note that these aren't always the ones set by default. Be sure to check your controls settings under `XRay` to find the correct keys*

- Press `Backslash` to toggle XRay `ON/OFF`
- Press `Z` to open the `selection & settings` Gui 

**Adding Blocks**

- Open the `selection & settings` Gui
- Select the method you'd like to use to add a block, either 
  - `From hand` *will setup the basic version of the block. So no axis, facing, etc*
  - `Looking At` *Will setup the complex version of the block, good for blocks you need specifc data from*
  - `Searching a list` *Like `From hand`, it will only setup a basic block*
- Set the Name, Color, and anything else you'd like to change
- Click add and Enable the Block if it's not enabled. You can enable and disable blocks by clicking on them in the Gui.

**Editing Blocks**

- Right click on any item in the Gui and edit as needed
- Click save and the changed will be applied instantly

## Previews

The [Imgur Album](http://imgur.com/a/23dX5)
![XRAY](http://i.imgur.com/N3KOEaE.png)

## Use on public servers

I **DO NOT** support the use of this mod on any public servers which do not allow this kind of mod. The mod **does** work on servers but I do not approve of, and will not, support anyone that attempts to use this mod on servers. I **do not** have the time to review each issue; I will simply close any issue with server connections in the crash log. 

If you wish to use this mod on private servers then that's on you. If you use this on public servers and are banned then that's on you and I will **not** support your use of this mod in that way. 

## A note on Optifine
Currently, the mod does not and has not work with Optifine since MC `1.7.x`. I am not sure why the two mods do not work 
together and due to Optfine being closed source I don't have the ability to investigate it properly. For now, I just recommend not using the two mods together. I hope to have it fixed soon. 

## Supports

A quick note on versioning. I try to support everything I can but with limited time I opt to discontinue every version **`2`** versions below the current game version. Example: if we are on `1.14.4` I will support `1.14.4` and `1.12.2` (we missed 1.13). If you find a very big bug in an old version I might be abe to resolve it if you submit a detailed bug report.

|Minecraft Version   | Mod Version | Branch | Author
|---|---|---|---
|1.16.1 | [2.3.1](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.16-v2.3.1) | [/1.16](https://github.com/MichaelHillcox/XRay-Mod/tree/1.16) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.15.2 | [2.2.0](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.15.2-v2.2.0) | [/1.15.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.15.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.15.1 | [2.1.0](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.15.1-v2.1.0) | [/1.15.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.15.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.14.4 | [2.0.4 *Limited support*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.14.4-v2.0.4) | [/1.14.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.14.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.12.2 | [1.5.0 *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.12.2-v1.5.0) | [/1.12.2](https://github.com/MichaelHillcox/XRay-Mod/tree/1.12.2) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.12.1 | [1.3.4 *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.12.1-v1.3.4) | [/1.12.1](https://github.com/MichaelHillcox/XRay-Mod/tree/1.12.1) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.12 | [1.3.3 *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.12-v1.3.3) | [/1.12.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.12.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.11.2 | [1.3.3 *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.11.2-v1.3.3) | [/1.11.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.11.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.10.2 | [1.3.1 *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.10.2-v1.3.1) | [/1.10.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.10.x) | [Michael Hillcox](https://github.com/MichaelHillcox)
|1.9.4 | [1.0.9 - *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.9.4-v1.0.9) | [/1.9.4](https://github.com/MichaelHillcox/XRay-Mod/tree/1.9.4) | [BondarenkoArtur](https://github.com/BondarenkoArtur) & [Michael Hillcox](https://github.com/MichaelHillcox)
|1.8.9 | [1.1.0 - *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.8.9-v1.1.0) | [/1.8.x](https://github.com/MichaelHillcox/XRay-Mod/tree/1.8.x) | [Michael Hillcox](https://github.com/MichaelHillcox) With help from  [BondarenkoArtur](https://github.com/BondarenkoArtur)
|1.7.10 | [1.0.1.75 - *Discontinued*](https://github.com/MichaelHillcox/XRay-Mod/releases/tag/1.0.1.75) | [/1.7.10](https://github.com/MichaelHillcox/XRay-Mod/tree/1.7.10) | [mcd1992](https://github.com/mcd1992) ([mcd1992 / GitLab](https://gitlab.com/mcd1992)) & [Michael Hillcox](https://github.com/MichaelHillcox)
|1.6.4 | 1.0.0 - *Discontinued* |  [/1.6.4](https://github.com/MichaelHillcox/XRay-Mod/tree/1.6.4) | [mcd1992](https://github.com/mcd1992) ([mcd1992 / GitLab](https://gitlab.com/mcd1992))

**Discontinued**: This means the mod is longer supported unless a game breaking bug is found.

**Limited support**: This means the mod will only receive bug fixes when ones are listed. I will no longer be back porting feature changes unless I get bored and have time to fix them.
