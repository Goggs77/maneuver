# Amethyst Maneuver

Amethyst Maneuver is a Fabric mod for Minecraft 26.2 that adds two
survival-oriented movement blocks:

- **Amethyst Gravity Anchor** applies a configurable gravity vector inside a
  rectangular 3D volume. Its editor follows the structure-block idea: choose an
  offset and size on each axis, set gravity in `g`, then apply.
- **Amethyst Damper** exponentially removes velocity from entities in a sphere.
  It tracks the downward squared speed it removes and uses that fraction to
  reduce fall damage at impact.

Both blocks are made from amethyst and are enabled/disabled from their
in-world editor. Creative and spectator players are ignored, and powered flight
is not changed.

## Gameplay rules

Gravity is stored as a vector in Earth-gravity units. `(0, -1, 0)` is normal
Minecraft gravity, `(0, 0, 0)` is weightless, and `(0, 1, 0)` pulls upward.
Components are limited to `-4..4 g` so a field cannot send entities beyond the
simulation's sane velocity range.

The gravity anchor only adds the difference between the configured vector and
vanilla gravity. Vanilla collisions, jumping, knockback, and fall processing
still run normally.

The damper computes:

```text
v_next = v_current * exp(-coefficient * radial_falloff * 0.05)
coefficient = strength * 3.5 s^-1
```

The strength field is entered as a percentage in the editor. The radial
falloff is linear from the center to the configured radius. Removed downward
speed squared is accumulated for the current fall. At impact:

```text
protected fraction = removed_v_y^2 / (removed_v_y^2 + impact_v_y^2)
fall damage distance = original fall distance * (1 - protected fraction)
```

The resulting distance is passed through vanilla fall damage, so armor,
enchantments, and other damage modifiers keep working.

## Multiplayer and physical sides

The mod is marked `environment: "*"` and splits its code into common and client
source sets:

- Common/server code owns blocks, items, block entities, menus, payloads,
  field simulation, networking, and fall-damage overrides.
- Client code owns editor screens, local movement prediction, and range
  rendering. No common class references `net.minecraft.client`.

Install the mod and Fabric API on both the dedicated server and every joining
Fabric client. The server remains authoritative and continues simulating
fields without a rendering client; clients add prediction, editor screens, and
range cues. As with any mod that adds blocks and items, vanilla clients cannot
join a server containing this content.

## Crafting

The gravity anchor uses four amethyst blocks, four amethyst shards, and a heavy
core from trial-chamber vaults. The damper uses four honeycomb, four wool of
any color, four amethyst shards, and one amethyst block. Both require an iron
pickaxe to drop.

## Project layout

```text
src/main/java/com/goggs/maneuver/
  block/        Block behavior and edit-screen entry points
  block/entity/ Server-side field simulation and persistence
  config/       Bounds, gravity, damper settings, and packet codecs
  mixin/        Fall-speed capture and fall-distance scaling
  net/          Validated client-to-server configuration payloads
  physics/      Pure math with no Minecraft dependency
  registry/     Blocks, items, block entities, menus, networking
src/client/java/com/goggs/maneuver/
  client/       In-world editors, local prediction, and range rendering
src/main/resources/
  assets/       Models, textures, and translations
  data/         Recipes, loot tables, tags, and recipe unlocks
scripts/        Asset generation, version pinning, and physics checks
```

## Build

The project expects JDK 25, matching Minecraft 26.2's runtime. This machine
already has JDK 25 inside the Minecraft launcher runtime, even though the
system `JAVA_HOME` points at JDK 21.

The verified build is already available at:

```text
build/libs/maneuver-1.0.5.jar
```

Install it in a Minecraft 26.2 Fabric profile with Fabric API. Rebuild it with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build.ps1
```

This uses the installed 26.2 game jar and Fabric API from the local launcher
profile, runs the physics/resource checks, compiles every class, and packages
the JAR. Minecraft 26.x is officially deobfuscated, so no remapping step is
needed.

For a conventional Loom build, first refresh the version pins, then run
Gradle:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\resolve-fabric-versions.ps1
$env:JAVA_HOME = "$env:APPDATA\.minecraft\runtime\java-runtime-epsilon"
.\gradlew.bat build
```

The resolver reads:

- <https://meta.fabricmc.net/v2/versions/game>
- <https://meta.fabricmc.net/v2/versions/loader/26.2>
- <https://api.modrinth.com/v2/project/fabric-api/version>
- <https://maven.fabricmc.net/net/fabricmc/fabric-loom/maven-metadata.xml>

Loom 1.19.1 requires Gradle 9.8.0. The first Loom build also needs to download
its build plugins from Fabric Maven.

## Verified smoke test

The packaged JAR was loaded by a headless Minecraft 26.2 Fabric server with
Fabric API. The mod initialized, mixins applied at `JAVA_25`, recipes and
advancements loaded, and both blocks were placed through server commands.
Their block entities reported the expected persisted fields:

```text
maneuver:gravity_field  offset/size and gravity vector
maneuver:damper         radius 6 and strength 0.55
```

## Range display

Both editors include a **Range: On/Off** toggle. When enabled, nearby players
see a particle outline without needing to look directly at the controller:

- Gravity fields show their rectangular box once the player's signed distance
  to the box is at most one third of its average side length.
- Dampers show three great-circle rings around their sphere once the player is
  within four thirds of the configured radius from its center.

Outline points are generated at a fixed density per block of edge length or
sphere circumference, so larger fields remain equally legible without
exhausting the particle budget.

Both editors also include **Creative: On/Off**. It defaults to off; when
enabled, the block affects creative players as well.

Field effects run once at the end of each level tick on both the client and
server, keeping the two simulations at the same phase.
Damping uses an exponential factor based on the 0.05-second tick interval, so
its speed transition is frame-rate independent and identical at low or high
FPS. Client-side ticking keeps local player prediction responsive while the
server applies the same effect authoritatively.

## Gravity-aware fall damage

Minecraft 26.2 computes fall damage from fall distance using:

```text
damage = floor((fallDistance + 0.000001 - safeFallDistance)
               * damageMultiplier * fallDamageMultiplier)
```

The gravity mixin keeps that formula, including the safe-fall attribute,
damage multiplier, damage multiplier attribute, and fall-immunity tag. It
replaces only the distance input when the entity was recently affected by a
gravity field.

For downward acceleration `a` in blocks/tick², Minecraft's discrete
gravity-and-drag motion has terminal velocity:

```text
terminal = 0.98 * a / (1 - 0.98) = 49a
velocity[n] = terminal * (1 - 0.98^n)
distance[n] = terminal * n - 0.98 * velocity[n] / (1 - 0.98)
```

Inverting that equation maps the impact speed back to the equivalent fall
distance under the current field acceleration. Zero or upward acceleration
uses vanilla gravity as the mapping fallback. The same formula is covered by
the offline physics checks.

## Offline checks

The movement formulas can be checked without downloading Minecraft:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test-physics.ps1
```

Validate every JSON, model, texture, recipe, and icon reference with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\validate-resources.ps1
```

Run both checks together with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check.ps1
```

Regenerate the pixel textures and mod icon with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\generate_textures.ps1
```

## Implementation notes

- Each anchor or damper ticks only while its block entity is loaded.
- Entity queries use the field's bounding box, not every block in the volume.
- Bounds are limited to 96 blocks per axis and offsets to `-64..64`.
- Client edits are re-clamped on the server and rejected outside 64 blocks.
- Multiple gravity anchors overlap additively. Plan overlapping fields as
  combined vectors.
- The damper intentionally affects airborne and grounded entities. Grounded
  damping is useful for slowing minecarts, item streams, and mob movement, but
  can make a floor of dampers feel sticky.
