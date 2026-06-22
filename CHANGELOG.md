# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.1.0] - 2026-06-22

### Added

- Initial release: `Bincode` implementation of `kotlinx.serialization.BinaryFormat` for the bincode binary format.
- Streaming encode/decode via Okio `Sink` / `Source` (`encodeToSink` / `decodeFromSource`).
- Configurable byte endianness, integer encoding (variable / fixed), trailing-byte policy, and size limit via the `Bincode { }` builder.
- Support for primitives, unsigned types, nullable values, `List`, `Map`, enums, value classes, and sealed types (sealed variants require an explicit `@SerialName("$SEALED_TAG;<index>")`).

[0.1.0]: https://github.com/QVSorrow/binkode/releases/tag/v0.1.0
