## [26.1.0.2]

### Added

- "Delete all" button to remove every block from the list, with a confirmation
- "Reset to defaults" button to restore the default ore list, with a confirmation
- Checkbox above the block list to enable or disable every block shown at once, respecting the current search
- The add block list now pins the block in your hand and the block you're looking at to the top
- Blocks already on your list are greyed out in the add block list and marked "Added", so they can't be added twice
- Hovering a block's icon in the add block list shows its normal item tooltip
- Messages for an empty list and for searches with no matches

### Changed

- Reworked the block list to be easier to use and more compact:
  - List entries are now smaller and long block names scroll instead of overflowing
  - Reset, delete all and close are now icon buttons at the top of the sidebar.
- Reworked the add block screen:
  - More compact layout with the search box at the top, a "Search..." hint and a close button beside it
  - Shows the mod's display name instead of its namespace, hover it to see the namespace
  - Pressing Enter in the search adds the top result
  - Your search and scroll position are kept when coming back from configuring a block
- Removed the "Add Block in hand" and "Add Looking at" buttons, the add block list covers both now
- Escape now goes back a screen instead of closing XRay entirely
- New blocks default to their map colour (iron is beige, gold is yellow...) instead of always blue
- The lava button now shows ON/OFF instead of true/false
- XRay now scans around the camera instead of the player, so it works with freecam style mods
- Improved rendering performance via various optimisations, especially when scanning large areas
  - Added Iris compatability to ensure our outlines are drawn correctly with Iris shader packs

### Fixed

- Random crash (`ConcurrentModificationException`) whilst moving around with XRay enabled (https://github.com/AdvancedXRay/XRay-Mod/issues/374)
- XRay outlines breaking apart and jumping around when very far from 0,0, such as near the world border [#295](https://github.com/AdvancedXRay/XRay-Mod/issues/295)
- The colour preview on the edit screen showing a missing texture instead of the transparency background
- The add block screen freezing the game for a few seconds in large modpacks
- Breaking an outlined block not removing its outline
- Outlines not following you when moving, especially when flying or teleporting
- Chunks that hadn't loaded yet being treated as empty, so their ores never showed up
- Outlines from the previous dimension staying visible after changing dimension
- Lava placed whilst XRay was on not being outlined
- XRay's fog settings leaking into other rendering

### Removed
- Removed the help screen and the hint text under the block list, the new list makes them redundant

## [26.1.0.1]

### Changed

- Updated to `26.1`

## [21.11.0]

### Changed

- Ported to Minecraft `1.21.11`

### Fixed

- Rendering lava blocks would not show correctly as they were rendering transparent
- Lava toggle button not working correctly
- Shaders should "just work"...

## [21.9.2]

### Fixed

- Hopefully address various shader issues with Iris specifically. Big thanks to all the users that helped as part of [#317](https://github.com/AdvancedXRay/XRay-Mod/issues/317)

## [21.9.1]

### Fixed

- Minecraft version range to allow for `1.21.9` alongside `1.21.10` versions.

## [21.9.0]

### Changed

- Updated to Minecraft 1.21.9

## [21.8.4]

### Added

- Added alpha channel to the color picker for blocks to allow for transparency. Thanks to [#329](https://github.com/AdvancedXRay/xray-mod/pull/329) by [@Groovin-Dev](https://github.com/Groovin-Dev)

## [21.8.3]

### Fixed

- Potential crash when rendering blocks that are broken.

## [21.8.2]

### Fixed

- Remove block button no longer showing on the edit block screen.

## [21.8.1]

### Fixed

- Small issue with state checking. If a block now changes state in-world, whilst the block is enabled, and it no longer matches, it will be removed from the outline render.
  - This is specifically tailored to blocks like `Suspicious Sand` and `Suspicious Gravel` which change state when brushed.

### Changed

- Lowered NeoForge minimum version to `1.21.7` to allow for `1.21.7` & `1.21.8` support

## [21.8.0]

### Changed

- Ported to `1.21.6`, `1.21.7`, `1.21.8`
- Basically rewrote most of the mod to improve performance
- All data is now stored in the `config/xray` folder instead of being in both the config and an `xray` folder
    - This means older configs will no longer work
- Block store has been improved to support categories but this is not yet implemented in the UI
  - Block store now supports `rgb`, `hsl`, `hex` and `0x` color formats for the `color` field
  - Support has been added for more scan targets but this is not yet implemented in the UI
- Reworked the scanning code to hopefully remove lag on scanning large areas
- Reworked the block rendering to use VBOs per chunk to improve performance
