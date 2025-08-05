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
