## [21.6.1]

### Fixed

- Crash on startup on newer versions of NeoForge

## [21.6.0]

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
