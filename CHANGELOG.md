# Changelog

## [0.2.1] - 2025-04-06

### Added
- More useful debug, logs and dialog messages

### Fixed
- Window not closed properly after the first relaunch

### Changed
- Disallowed Java installs below Java 21 from being detected
- Disallowed Java installs below Java 21 from working

## [0.2.0] - 2025-04-04

### Added
- Auto Java Detection
- Java 9+ Process API usage to check if parent process is alive, much more reliable than older method
- Certs fix for older Java 8 users
- Relauncher now pops up if a newer Cleanroom version is fetched

### Changed
- Configuration now moved to `config/relauncher.json`

### Fixed
- Various GUI scaling, position issues

## [0.1.2] - 2025-03-13

### Fixed
- Malformed jar + wrapper locations being used

## [0.1.1] - 2025-03-12

### Added
- Wrapper class as entrypoint to ensure the relaunched process exits properly when the original process is killed
- Dropdown selector for Cleanroom versions
- Java path selector with a file chooser dialog
- Logo as icon
- Focusing on text field and un-focusing working
- Log when relauncher is exited without specifying a release
- Overhauled component sizing/scaling thanks to @tttsaurus

### Changed
- Relauncher window now has a system conforming look and feel

### Fixed
- The relauncher now appears on the taskbar

## [0.1.0] - 2025-03-07

### Added
- Relaunches Cleanroom's major versions