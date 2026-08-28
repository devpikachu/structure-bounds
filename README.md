# Structure Bounds

A Paper server plugin for Minecraft, published under the LGPL3-or-later license.

## Requirements

The plugin reaches into the server's own internals, so it is not guaranteed to work on other releases. Anything outside
this table is best-effort and logged on startup.

| Structure Bounds | Minecraft |
|------------------|-----------|
| 0.1.0+           | 1.21.11   |

## Installation

Drop `structure-bounds-<version>.jar` into the server's `plugins/` directory and restart.

## Configuration

`plugins/structure-bounds/config.yml`.

| Key     | Description                                                | Default |
|---------|------------------------------------------------------------|---------|
| `debug` | Enables debug functionality. See [Debug Mode](#debug-mode) | `false` |

## Debug Mode

Everything gated behind the `debug` configuration flag, which is off by default.

## Contributing

Contributions are welcome, whether that is a bug report, a compatibility finding from your own server, a feature
request, or a pull request.

Everyone taking part is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

This project is licensed under [LGPL3-or-later](LICENSE).
