# Structure Bounds

One command outlines every generated structure around you, and every piece inside it, so you can see where a village
ends, where a mineshaft runs, and exactly which pieces the world generator placed.

> [!IMPORTANT]
> Only chunks that are already loaded are scanned, and the outlines are rebuilt when you cross a chunk boundary. A
> structure nobody has loaded is not drawn, and one that loads while you stand still appears once you move.

## Features

- **See the shape of a structure:** a white box around the structure itself, a green one around its start piece and a
  blue one around every other piece, the colours vanilla's own debug view uses
- **Named pieces:** each drawn piece carries its name above it, readable through terrain so a buried piece still reads
- **Isolate one structure:** keep the structure you are standing in, drop every other, and give the one you kept a glow
  so it stays visible from outside
- **Nobody else sees them:** the boxes are packets rather than blocks or entities, so nothing is spawned, nothing
  persists a restart and no other player is sent them
- **Tunable detail:** each player picks how many pieces a structure may draw, or lifts the limit entirely, and what a
  limit drops is always the furthest away
- **Configurable limits:** operators set the scan radius and a cap on how many boxes a single player can be sent
- **Settings persist:** every toggle is stored on the player, so it survives a relog

## Requirements

The plugin needs Paper, or a fork of it, and reaches into the server's own internals, so it is not guaranteed to work on
other releases. Anything outside this table is best-effort and logged on startup.

| Structure Bounds | Minecraft |
|------------------|-----------|
| 0.1.0+           | 1.21.11   |

## Installation

Drop `structure-bounds-<version>.jar` into the server's `plugins/` directory and restart.

## Commands

| Command                      | Description                                                                | Permission             |
|------------------------------|----------------------------------------------------------------------------|------------------------|
| `/bounds`                    | Draws the outlines around you, or hides them again                         | `structure-bounds.use` |
| `/bounds all-pieces`         | Draws every piece whatever the `max-pieces` limit, or applies it again     | `structure-bounds.use` |
| `/bounds isolate`            | Keeps only the structure you are standing in, or clears the hold           | `structure-bounds.use` |
| `/bounds max-pieces <count>` | Sets how many pieces one structure may draw, up to `max-boxes-per-player`  | `structure-bounds.use` |
| `/bounds show-labels`        | Draws each piece's name above it, or hides them                            | `structure-bounds.use` |

A command that redraws is limited to one every two seconds, and refuses with a message until the wait is out. Hiding
the outlines is never refused.

## Configuration

`plugins/structure-bounds/config.yml`.

| Key                    | Description                                                | Default | Minimum | Maximum |
|------------------------|------------------------------------------------------------|---------|---------|---------|
| `debug`                | Enables debug functionality. See [Debug Mode](#debug-mode) | `false` |         |         |
| `scan-radius-chunks`   | How far around a player to look for structures, in chunks  | `6`     | `1`     | `16`    |
| `max-boxes-per-player` | How many boxes one player may be sent at once              | `256`   | `1`     | `1024`  |

Out-of-range values are clamped back to their default, with a warning in the console.

A player who is sent the cap keeps the boxes nearest them, loses the rest, and is told once that the view was cut
short.

## Permissions

| Node                   | Grants                        | Default  |
|------------------------|-------------------------------|----------|
| `structure-bounds.use` | `/bounds` and its subcommands | Everyone |

## Debug Mode

Everything here is gated behind the `debug` configuration flag, which is off by default.

Every scan, redraw, dropped session and refused command is logged to the console, naming the player and the counts
involved. Nothing a player sees changes.

## Contributing

Contributions are welcome, whether that is a bug report, a compatibility finding from your own server, a feature
request, or a pull request.

Everyone taking part is expected to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## License

This project is licensed under [LGPL3-or-later](LICENSE).
