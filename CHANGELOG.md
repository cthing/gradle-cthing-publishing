# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [unreleased]

## [3.0.0] - 2025-09-26

### Removed

- Site URL has been removed because C Thing Software project no longer generate
  a project site.

### Fixed

- Calling `CThingPublishingExtension.createPomAction()` no longer causes a configuration
  mutation error when using Gradle 9.0.0 or newer.

## [2.0.0] - 2025-02-18

### Added

- Developers can now be added and replaced
- The POM `scm` section is now populated directly from the `remoteUrl` in the
  `.git/config` file

### Removed

- The `CThingPomAction.user` property is no longer needed and has been removed 

## [1.0.0] - 2025-02-01

### Added

- First release

[unreleased]: https://github.com/cthing/gradle-cthing-publishing/compare/3.0.0...HEAD
[3.0.0]: https://github.com/cthing/gradle-cthing-publishing/releases/tag/3.0.0
[2.0.0]: https://github.com/cthing/gradle-cthing-publishing/releases/tag/2.0.0
[1.0.0]: https://github.com/cthing/gradle-cthing-publishing/releases/tag/1.0.0
