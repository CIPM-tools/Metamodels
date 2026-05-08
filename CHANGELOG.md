# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

### Changed

### Deprecated

### Removed

### Fixed

### Security

## [1.0.1] - 2026-05-08

### Fixed

* The Instrumentation Metamodel (1.0.1) contains metadata for the Eclipse IDE so that it can find the included metamodel.

## [1.0.0] - 2026-05-03

Initial release, which adds these features with their own versions:

* Lua Grammar and Metamodel 0.1.0
    * Xtext grammar for Lua 5.1 based on [Melange](https://melange.inria.fr/)
    * Corresponding Lua metamodel
    * Generated parser for creating Lua models from Lua source code
    * Generated serializer for creating Lua source code from Lua models
    * Resolution of references within a Lua model (incomplete)
* Instrumentation Metamodel 1.0.0
    * Enables modeling of instrumentation points for SEFF actions in the [Palladio Component Model](https://github.com/PalladioSimulator)
* Supported Java version: Java 11
* Supported Eclipse Modeling Tools IDE version: 2022-09

[Unreleased]: https://github.com/CIPM-tools/Metamodels/compare/releases/v1.0.1...HEAD
[1.0.1]: https://github.com/CIPM-tools/Metamodels/releases/tag/v1.0.1
[1.0.0]: https://github.com/CIPM-tools/Metamodels/releases/tag/v1.0.0
