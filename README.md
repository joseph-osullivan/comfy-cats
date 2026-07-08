<p align="center">
  <img width="224" height="187" alt="comfy_cats_icon" src="https://github.com/user-attachments/assets/1715c711-cb41-41e9-8268-fa528fa44fcf" />
</p>

# Comfy Cats

A tiny Fabric mod for **Minecraft 26.3-snapshot-3**: during the day, tamed cats
curl up and nap on [cushions](https://minecraft.wiki/w/Cushion), reusing the same
lying-down pose they already use on beds.

## What it does

- A tamed cat that isn't ordered to sit looks for a nearby cushion in daylight.
- It walks over at a calm pace, settles **on top** of the cushion, and lies down
  using the vanilla "cat on a bed" pose — facing whoever is watching it.
- After a short nap it gets up and wanders off for a while before another cushion
  tempts it, much like a cat's on-and-off relationship with a bed.
- At night, or if the cushion is broken or already taken, the cat gets up and
  goes about its business.

## How it works

A cushion isn't a block — it's an **entity** with no collision that you can punch
off a wall — so vanilla's `CatSitOnBlockGoal` (which scans for block positions)
never notices it. This mod adds a small custom AI goal, `CatLieOnCushionGoal`, and
injects it into every cat with a mixin.

To sit *on top* of the cushion rather than sinking into it, the cat **rides** the
cushion the same way a player does; vanilla's passenger placement puts it neatly on
the seat. A cat saved mid-nap is dismounted on load, so a passenger that desyncs
with the cushion's lazy networking can't leave it frozen. A small client-side render
tweak turns the napping cat to face the viewer, since passengers don't sync body
yaw.

The whole mod is three short classes and touches only vanilla — no Fabric API
dependency.

## Building

Requires **JDK 25**.

```sh
./gradlew build
```

The built jar lands in `build/libs/`. Drop it into a Fabric 26.3-snapshot-3
instance alongside [Fabric Loader](https://fabricmc.net/).

## License

MIT — see [LICENSE](LICENSE).
